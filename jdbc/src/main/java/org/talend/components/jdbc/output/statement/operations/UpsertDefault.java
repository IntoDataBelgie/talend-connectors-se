/*
 * Copyright (C) 2006-2023 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.jdbc.output.statement.operations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.jdbc.configuration.OutputConfig;
import org.talend.components.jdbc.output.OutputUtils;
import org.talend.components.jdbc.output.Reject;
import org.talend.components.jdbc.output.platforms.Platform;
import org.talend.components.jdbc.output.statement.RecordToSQLTypeConverter;
import org.talend.components.jdbc.service.I18nMessage;
import org.talend.components.jdbc.service.JdbcService;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

@Slf4j
@Getter
public class UpsertDefault extends QueryManagerImpl {

    private final Insert insert;

    private final Update update;

    private final List<String> keys;

    private Map<Integer, Schema.Entry> queryParams;

    public UpsertDefault(final Platform platform, final OutputConfig configuration, final I18nMessage i18n) {
        super(platform, configuration, i18n);
        this.keys = new ArrayList<>(ofNullable(configuration.getKeys()).orElse(emptyList()));
        if (this.keys.isEmpty()) {
            throw new IllegalArgumentException(i18n.errorNoKeyForUpdateQuery());
        }
        insert = new Insert(platform, configuration, i18n);
        update = new Update(platform, configuration, i18n);
    }

    @Override
    public String buildQuery(final List<Record> records) {
        this.queryParams = new HashMap<>();
        final AtomicInteger index = new AtomicInteger(0);
        final List<Schema.Entry> entries = OutputUtils.getAllSchemaEntries(records);

        return "SELECT COUNT(*) AS RECORD_EXIST FROM "
                + getPlatform().identifier(getConfiguration().getDataset().getTableName())
                + " WHERE "
                + getConfiguration()
                        .getKeys()
                        .stream()
                        .peek(key -> queryParams
                                .put(index.incrementAndGet(),
                                        entries
                                                .stream()
                                                .filter(e -> e.getOriginalFieldName().equals(key))
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalStateException(
                                                        getI18n().errorNoFieldForQueryParam(key)))))
                        .map(c -> getPlatform().identifier(c))
                        .map(c -> c + " = ?")
                        .collect(joining(" AND "));
    }

    @Override
    public boolean validateQueryParam(final Record rec) {
        final Set<Schema.Entry> entries = new HashSet<>(rec.getSchema().getEntries());
        return keys.stream().allMatch(k -> entries.stream().anyMatch(entry -> entry.getOriginalFieldName().equals(k)))
                && entries
                        .stream()
                        .filter(entry -> keys.contains(entry.getOriginalFieldName()))
                        .filter(entry -> !entry.isNullable())
                        .map(entry -> valueOf(rec, entry))
                        .allMatch(Optional::isPresent);
    }

    @Override
    public Map<Integer, Schema.Entry> getQueryParams() {
        return queryParams;
    }

    @Override
    public List<Reject> execute(final List<Record> records, final JdbcService.JdbcDatasource dataSource)
            throws SQLException {
        if (records.isEmpty()) {
            return emptyList();
        }
        final List<Record> needUpdate = new ArrayList<>();
        final List<Record> needInsert = new ArrayList<>();
        final String query = buildQuery(records);
        final List<Reject> discards = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement(query)) {
                for (final Record rec : records) {
                    statement.clearParameters();
                    if (!validateQueryParam(rec)) {
                        discards.add(new Reject("missing required query param in this record", rec));
                        continue;
                    }
                    for (final Map.Entry<Integer, Schema.Entry> entry : getQueryParams().entrySet()) {
                        RecordToSQLTypeConverter
                                .valueOf(entry.getValue().getType().name())
                                .setValue(statement, entry.getKey(),
                                        entry.getValue(), rec);
                    }
                    try (final ResultSet result = statement.executeQuery()) {
                        if (result.next() && result.getInt("RECORD_EXIST") > 0) {
                            needUpdate.add(rec);
                        } else {
                            needInsert.add(rec);
                        }
                    }
                }
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } catch (final SQLException e) {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                throw e;
            }
        }

        // fixme handle the update and insert in // need a pool of 2 !
        if (!needInsert.isEmpty()) {
            insert.buildQuery(needInsert);
            discards.addAll(insert.execute(needInsert, dataSource));
        }
        if (!needUpdate.isEmpty()) {
            update.buildQuery(needUpdate);
            discards.addAll(update.execute(needUpdate, dataSource));
        }

        return discards;
    }
}
