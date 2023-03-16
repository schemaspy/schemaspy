package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.input.dbms.config.PropertiesResolver;

import java.util.Objects;
import java.util.Properties;

@Parameters(resourceBundle = "databasetypeconfigcli")
public class DatabaseTypeConfigCli implements DatabaseTypeConfig {

    @Parameter(
        names = {
            "-t", "--database-type",
            "schemaspy.t", "schemaspy.database-type"
        },
        descriptionKey = "database-type"
    )
    private String type = "ora";

    private final PropertiesResolver propertiesResolver;
    private Properties properties;

    public DatabaseTypeConfigCli(PropertiesResolver propertiesResolver) {
        this.propertiesResolver = propertiesResolver;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public synchronized Properties getProperties() {
        if (Objects.isNull(properties)) {
            properties = propertiesResolver.getDbProperties(getType());
        }
        return properties;
    }
}
