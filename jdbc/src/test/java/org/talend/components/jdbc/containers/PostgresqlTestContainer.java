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
package org.talend.components.jdbc.containers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.experimental.Delegate;

public class PostgresqlTestContainer implements JdbcTestContainer {

    @Delegate(types = { DelegatedMembers.class })
    private final JdbcDatabaseContainer container = new PostgreSQLContainer("postgres:12.1");

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public void close() {
        this.container.close();
    }
}
