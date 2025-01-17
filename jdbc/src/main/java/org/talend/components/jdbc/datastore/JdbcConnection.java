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
package org.talend.components.jdbc.datastore;

import static org.talend.components.jdbc.service.UIActionService.ACTION_LIST_HANDLERS_DB;
import static org.talend.components.jdbc.service.UIActionService.ACTION_LIST_SUPPORTED_DB;
import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.AND;
import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.OR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.talend.components.jdbc.configuration.JdbcConfiguration.KeyVal;
import org.talend.components.jdbc.migration.JdbcConnectionMigrationHandler;
import org.talend.components.jdbc.service.UIActionService;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.action.Proposable;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.condition.ActiveIfs;
import org.talend.sdk.component.api.configuration.constraint.Min;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;
import lombok.ToString;

@Data
@Version(value = JdbcConnection.VERSION, migrationHandler = JdbcConnectionMigrationHandler.class)
@ToString(exclude = { "password", "privateKey", "privateKeyPassword" })
@GridLayout({ @GridLayout.Row({ "dbType", "handler" }), @GridLayout.Row("setRawUrl"), @GridLayout.Row("jdbcUrl"),
        @GridLayout.Row("host"), @GridLayout.Row("port"), @GridLayout.Row("database"), @GridLayout.Row("parameters"),
        @GridLayout.Row("authenticationType"), @GridLayout.Row("userId"), @GridLayout.Row("password"),
        @GridLayout.Row("privateKey"), @GridLayout.Row("privateKeyPassword"), @GridLayout.Row("oauthTokenEndpoint"),
        @GridLayout.Row("clientId"), @GridLayout.Row("clientSecret"), @GridLayout.Row("grantType"),
        @GridLayout.Row("oauthUsername"), @GridLayout.Row("oauthPassword"), @GridLayout.Row("scope") })
@GridLayout(names = GridLayout.FormType.ADVANCED, value = { @GridLayout.Row({ "defineProtocol", "protocol" }),
        @GridLayout.Row("connectionTimeOut"), @GridLayout.Row("connectionValidationTimeOut") })
@DataStore("JdbcConnection")
@Checkable(UIActionService.ACTION_BASIC_HEALTH_CHECK)
@Documentation("A connection to a data base.")
public class JdbcConnection implements Serializable {

    public static final int VERSION = 3;

    @Option
    @Required
    @Documentation("Data base type from the supported data base list.")
    @Proposable(ACTION_LIST_SUPPORTED_DB)
    private String dbType;

    @Option
    @ActiveIf(target = "dbType", value = { "Aurora", "SingleStore" })
    @Documentation("Database handlers, this configuration is for cloud databases that support the use of other databases drivers.")
    @Suggestable(value = ACTION_LIST_HANDLERS_DB, parameters = { "dbType" })
    private String handler;

    @Option
    @Documentation("Let user define complete jdbc url or not")
    @DefaultValue("false")
    private Boolean setRawUrl = false;

    @Option
    @ActiveIf(target = "setRawUrl", value = { "true" })
    @Documentation("jdbc connection raw url")
    private String jdbcUrl;

    @Option
    @ActiveIf(target = "setRawUrl", value = { "false" })
    @Documentation("jdbc host")
    private String host;

    @Option
    @ActiveIf(target = "setRawUrl", value = { "false" })
    @Documentation("jdbc port")
    @DefaultValue("80")
    private int port = 80;

    @Option
    @ActiveIf(target = "setRawUrl", value = { "false" })
    @Documentation("jdbc database")
    private String database;

    @Option
    @ActiveIf(target = "setRawUrl", value = { "false" })
    @Documentation("jdbc parameters")
    private List<KeyVal> parameters = new ArrayList<>();

    @Option
    @Documentation("Let user define protocol of the jdbc url.")
    @DefaultValue("false")
    @ActiveIf(target = "setRawUrl", value = { "false" })
    private Boolean defineProtocol = false;

    @Option
    @ActiveIfs(value = { @ActiveIf(target = "setRawUrl", value = { "false" }),
            @ActiveIf(target = "defineProtocol", value = { "true" }) })
    @Documentation("Protocol")
    private String protocol;

    @Option
    @DefaultValue("BASIC")
    @ActiveIf(target = "dbType", value = "Snowflake")
    @Documentation("Authentication type.")
    private AuthenticationType authenticationType;

    @Option
    @ActiveIfs(value = { @ActiveIf(target = "dbType", value = "Snowflake", negate = true),
            @ActiveIf(target = "authenticationType", value = "OAUTH", negate = true) }, operator = OR)
    @Documentation("database user.")
    private String userId;

    @Option
    @ActiveIfs(value = { @ActiveIf(target = "dbType", value = "Snowflake", negate = true),
            @ActiveIf(target = "authenticationType", value = "BASIC") }, operator = OR)
    @Credential
    @Documentation("database password.")
    private String password;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "KEY_PAIR") })
    @Credential
    @Documentation("Private key.")
    private String privateKey;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "KEY_PAIR") })
    @Credential
    @Documentation("Private key password.")
    private String privateKeyPassword;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH") })
    @Documentation("Oauth token endpoint.")
    private String oauthTokenEndpoint;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH") })
    @Documentation("Client ID.")
    private String clientId;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH") })
    @Credential
    @Documentation("Client secret.")
    private String clientSecret;

    @Option
    @DefaultValue("CLIENT_CREDENTIALS")
    @ActiveIfs(value = { @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH") }, operator = AND)
    @Documentation("Grant type.")
    private GrantType grantType;

    @Option
    @ActiveIfs(value = { @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH"),
            @ActiveIf(target = "grantType", value = "PASSWORD") }, operator = AND)
    @Documentation("OAuth username.")
    private String oauthUsername;

    @Option
    @ActiveIfs(value = { @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH"),
            @ActiveIf(target = "grantType", value = "PASSWORD") }, operator = AND)
    @Credential
    @Documentation("OAuth password.")
    private String oauthPassword;

    @Option
    @ActiveIfs({ @ActiveIf(target = "dbType", value = "Snowflake"),
            @ActiveIf(target = "authenticationType", value = "OAUTH") })
    @Documentation("Scope.")
    private String scope;

    @Min(0)
    @Option
    @Documentation("Set the maximum number of seconds that a client will wait for a connection from the pool. "
            + "If this time is exceeded without a connection becoming available, a SQLException will be thrown from DataSource.getConnection().")
    private long connectionTimeOut = 30;

    @Min(0)
    @Option
    @Documentation("Sets the maximum number of seconds that the pool will wait for a connection to be validated as alive.")
    private long connectionValidationTimeOut = 10;

    public void setJdbcUrl(final String jdbcUrl) {
        this.setSetRawUrl(true);
        this.jdbcUrl = jdbcUrl;
        this.setHost("");
        this.setPort(80);
        this.setDatabase("");
        this.setParameters(new ArrayList<>());
    }

}
