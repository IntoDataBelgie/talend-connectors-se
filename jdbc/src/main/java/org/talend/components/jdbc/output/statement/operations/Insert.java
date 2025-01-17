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

import lombok.extern.slf4j.Slf4j;
import org.talend.components.jdbc.configuration.OutputConfig;
import org.talend.components.jdbc.output.OutputUtils;
import org.talend.components.jdbc.output.platforms.Platform;
import org.talend.components.jdbc.service.I18nMessage;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

@Slf4j
public class Insert extends QueryManagerImpl {

    private Map<Integer, Schema.Entry> namedParams;

    private final Map<String, String> queries = new HashMap<>();

    public Insert(final Platform platform, final OutputConfig configuration, final I18nMessage i18n) {
        super(platform, configuration, i18n);
    }

    @Override
    public String buildQuery(final List<Record> records) {
        final List<Schema.Entry> entries = OutputUtils.getAllSchemaEntries(records);

        return queries.computeIfAbsent(entries.stream().map(Schema.Entry::getOriginalFieldName).collect(joining("::")),
                key -> {
                    final AtomicInteger index = new AtomicInteger(0);
                    namedParams = new HashMap<>();
                    entries.forEach(name -> namedParams.put(index.incrementAndGet(), name));
                    final List<Map.Entry<Integer, Schema.Entry>> params = namedParams
                            .entrySet()
                            .stream()
                            .sorted(comparing(Map.Entry::getKey))
                            .collect(toList());
                    final StringBuilder query = new StringBuilder("INSERT INTO ")
                            .append(getPlatform().identifier(getConfiguration().getDataset().getTableName()));
                    query
                            .append(params
                                    .stream()
                                    .map(e -> getConfiguration().isUseOriginColumnName()
                                            ? e.getValue().getOriginalFieldName()
                                            : e.getValue().getName())
                                    .map(name -> getPlatform().identifier(name))
                                    .collect(joining(",", "(", ")")));
                    query.append(" VALUES");
                    query.append(params.stream().map(e -> "?").collect((joining(",", "(", ")"))));
                    return query.toString();
                });
    }

    @Override
    public boolean validateQueryParam(final Record rec) {
        return namedParams
                .values()
                .stream()
                .filter(e -> !e.isNullable())
                .map(e -> valueOf(rec, e))
                .allMatch(Optional::isPresent);
    }

    @Override
    public Map<Integer, Schema.Entry> getQueryParams() {
        return namedParams;
    }
}