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
package org.talend.components.jdbc.platforms;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.talend.components.jdbc.common.JDBCConfiguration;
import org.talend.components.jdbc.schema.Dbms;
import org.talend.components.jdbc.service.I18nMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * https://docs.microsoft.com/fr-fr/sql/t-sql/statements/create-table-transact-sql?view=sql-server-2017
 */
@Slf4j
public class MSSQLPlatform extends Platform {

    public static final String MSSQL = "mssql";

    public static final String MSSQL_JTDS = "mssql_jtds";

    @Override
    protected String buildUrlFromPattern(final String protocol, final String host, final int port,
            final String database,
            String params) {
        if (!"".equals(params.trim())) {
            params = ";" + params;
        }
        return String.format("%s://%s:%s;databaseName=%s%s", protocol, host, port, database, params.replace('&', ';'));
    }

    public MSSQLPlatform(final I18nMessage i18n, final JDBCConfiguration.Driver driver) {
        super(i18n, driver);
    }

    @Override
    public String name() {
        return MSSQL;
    }

    @Override
    public String delimiterToken() {
        return "\"";
    }

    @Override
    protected String buildQuery(final Connection connection, final Table table, final boolean useOriginColumnName,
            Dbms mapping)
            throws SQLException {
        // keep the string builder for readability
        final StringBuilder sql = new StringBuilder("CREATE TABLE");
        sql.append(" ");
        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            sql.append(identifier(table.getSchema())).append(".");
        }
        sql.append(identifier(table.getName()));
        sql.append("(");
        sql.append(createColumns(table.getColumns(), useOriginColumnName, mapping));
        sql
                .append(createPKs(connection.getMetaData(), table.getName(),
                        table.getColumns().stream().filter(Column::isPrimaryKey).collect(Collectors.toList())));
        sql.append(")");
        // todo create index

        log.debug("### create table query ###");
        log.debug(sql.toString());
        return sql.toString();
    }

    @Override
    protected boolean isTableExistsCreationError(final Throwable e) {
        return e instanceof SQLException && "S0001".equalsIgnoreCase(((SQLException) e).getSQLState())
                && 2714 == ((SQLException) e).getErrorCode();
    }

    @Override
    protected String isRequired(final Column column) {
        return column.isNullable() && !column.isPrimaryKey() ? "" : "NOT NULL";
    }

    @Override
    public void addDataSourceProperties(HikariDataSource dataSource) {
        super.addDataSourceProperties(dataSource);

        // https://docs.microsoft.com/en-us/sql/connect/jdbc/setting-the-connection-properties?view=sql-server-2017
        dataSource.addDataSourceProperty("applicationName", APPLICATION);
    }
}
