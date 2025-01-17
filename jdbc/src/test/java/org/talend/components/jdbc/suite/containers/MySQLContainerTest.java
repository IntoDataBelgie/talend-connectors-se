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
package org.talend.components.jdbc.suite.containers;

import org.junit.jupiter.api.Tag;
import org.talend.components.jdbc.containers.JdbcTestContainer;
import org.talend.components.jdbc.containers.MySQLTestContainer;
import org.talend.components.jdbc.suite.JDBCBaseContainerTest;

@Tag("IT")
public class MySQLContainerTest extends JDBCBaseContainerTest {

    @Override
    public JdbcTestContainer buildContainer() {
        return new MySQLTestContainer();
    }

    public class MySQLPlatform extends JDBCBaseContainerTest.PlatformTests {
    }

    public class MySQLInput extends JDBCBaseContainerTest.InputTest {
    }

    public class MySQLOutput extends JDBCBaseContainerTest.OutputTest {
    }

    public class MySQLUI extends JDBCBaseContainerTest.UIActionServiceTest {
    }

}
