package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.util.DbSpecificConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Parameters(resourceBundle = "connectionconfigcli")
public class ConnectionConfigCli implements ConnectionConfig {

    @Parameter(
        names = {
            "-db", "--database-name",
            "schemaspy.db", "schemaspy.database-name"
        },
        descriptionKey = "databaseName"
    )
    private String databaseName;

    @Parameter(
        names = {
            "-host", "--host",
            "schemaspy.host"
        },
        descriptionKey = "host"
    )
    private String host;

    @Parameter(
        names = {
            "-port", "--port",
            "schemaspy.port"
        },
        descriptionKey = "port"
    )
    private Integer port;

    @Parameter(
        names = {
            "-u", "--user",
            "schemaspy.u", "schemaspy.user"},
        descriptionKey = "user"
    )
    private String user;

    @Parameter(
        names = {
            "-p", "--password",
            "schemaspy.pw", "schemaspy.password"
        },
        descriptionKey = "password"
    )
    private String password;

    @Parameter(
        names = {
            "-pfp", "--prompt-for-password",
            "schemaspy.pfp"
        },
        descriptionKey = "pfp",
        password = true
    )
    private String passwordFromPrompt;

    @Parameter(
        names = {
            "-connprops",
            "schemaspy.connprops"
        },
        descriptionKey = "connprops"
    )
    private String connprops = null;

    @Parameter(
        names = {
            "-dp", "--driverPath",
            "schemaspy.dp", "schemaspy.driverPath"
        },
        descriptionKey = "driverPath"
    )
    private String driverPath;

    @Parameter(
        names = {
            "-loadjars","--load-sibling-jars",
            "schemaspy.loadjars", "schemaspy.load-sibling-jars"
        },
        descriptionKey = "loadsiblings"
    )
    private boolean loadSiblings = false;

    private final DatabaseTypeConfig databaseTypeConfig;
    private List<String> remainingArguments;

    public ConnectionConfigCli(DatabaseTypeConfig databaseTypeConfig) {
        this.databaseTypeConfig = databaseTypeConfig;
    }

    @Override
    public String getDatabaseType() {
        return databaseTypeConfig.getType();
    }

    @Override
    public Properties getDatabaseTypeProperties() {
        return databaseTypeConfig.getProperties();
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        if (Objects.nonNull(passwordFromPrompt)) {
            return passwordFromPrompt;
        }
        return password;
    }

    @Override
    public String getConnectionProperties() {
        return connprops;
    }

    @Override
    public DbSpecificConfig getDbSpecificConfig() {
        return new DbSpecificConfig(getDatabaseType(), getDatabaseTypeProperties());
    }

    @Override
    public String getDriverPath() {
        return driverPath;
    }

    @Override
    public boolean withLoadSiblings() {
        return loadSiblings;
    }

    public void setRemainingArguments(List<String> remainingArguments) {
        this.remainingArguments = remainingArguments;
    }

    @Override
    public List<String> getRemainingArguments() {
        return new ArrayList<>(remainingArguments);
    }
}
