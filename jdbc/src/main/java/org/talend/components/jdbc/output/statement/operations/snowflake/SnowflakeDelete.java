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
package org.talend.components.jdbc.output.statement.operations.snowflake;

import org.talend.components.jdbc.configuration.OutputConfig;
import org.talend.components.jdbc.output.Reject;
import org.talend.components.jdbc.output.platforms.Platform;
import org.talend.components.jdbc.output.statement.operations.Delete;
import org.talend.components.jdbc.service.I18nMessage;
import org.talend.components.jdbc.service.JdbcService;
import org.talend.components.jdbc.service.SnowflakeCopyService;
import org.talend.sdk.component.api.record.Record;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeDelete extends Delete {

    SnowflakeCopyService snowflakeCopy = new SnowflakeCopyService();

    public SnowflakeDelete(Platform platform, OutputConfig configuration, I18nMessage i18n) {
        super(platform, configuration, i18n);
        snowflakeCopy.setUseOriginColumnName(configuration.isUseOriginColumnName());
    }

    @Override
    public List<Reject> execute(final List<Record> records, final JdbcService.JdbcDatasource dataSource)
            throws SQLException {
        buildQuery(records);
        final List<Reject> rejects = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection()) {
            final String tableName = getConfiguration().getDataset().getTableName();
            final String tmpTableName = snowflakeCopy.tmpTableName(tableName);
            final String fqTableName = namespace(connection) + "." + getPlatform().identifier(tableName);
            final String fqTmpTableName = namespace(connection) + "." + getPlatform().identifier(tmpTableName);
            final String fqStageName = namespace(connection) + ".%" + getPlatform().identifier(tmpTableName);
            rejects.addAll(
                    snowflakeCopy.putAndCopy(connection, records, fqStageName, fqTableName, fqTmpTableName, true));
            if (records.size() != rejects.size()) {
                try (final Statement statement = connection.createStatement()) {
                    String query = "delete from " + fqTableName + " target using " + fqTmpTableName
                            + " as source where "
                            + getConfiguration()
                                    .getKeys()
                                    .stream()
                                    .map(key -> getPlatform().identifier(key))
                                    .map(key -> "source." + key + "= target." + key)
                                    .collect(joining(" AND "));
                    log.debug("Delete query: {}", query);
                    statement.execute(query);
                }
            }
            connection.commit();
        } finally {
            snowflakeCopy.cleanTmpFiles();
        }
        return rejects;
    }
}
