package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

/**
 * Holds all supported command line arguments.
 * <p>
 * An instance of this class registered as singleton bean in {@link org.schemaspy.SchemaSpyConfiguration} via {@link CommandLineArgumentParser}.
 * Therefore it can be injected in other beans, for example:
 * <p>
 * <pre>
 *     public class MyService {
 *         private CommandLineArguments arguments;
 *
 *         // use constructor injection
 *         public MyService(CommandLineArguments arguments) {
 *             this.arguments = arguments
 *         }
 *     }
 * </pre>
 * <p>
 * TODO migrate other command line parameter from {@link org.schemaspy.Config}
 */
@Parameters(resourceBundle = "commandlinearguments")
public class CommandLineArguments {

    @Parameter(names = {
            "?", "-?", "/?",
            "-h",
            "help", "-help", "--help"},
            descriptionKey = "help",
            help = true,
            order = 1
    )
    private boolean helpRequired;

    @Parameter(
            names = {
                    "-dbHelp", "-dbhelp",
                    "--dbHelp", "--dbhelp"
            },
            help = true,
            descriptionKey = "dbhelp",
            order = 2
    )
    private boolean dbHelpRequired;

    @Parameter(
            names = {"-debug", "--debug", "debug", "schemaspy.debug"},
            descriptionKey = "debug"
    )
    private boolean debug = false;

    @Parameter(
            names = {
                    "-t", "--database-type", "database-type",
                    "schemaspy.t", "schemaspy.database-type"
            },
            descriptionKey = "database-type"
    )
    private String databaseType = "ora";

    @Parameter(
            names = {
                    "-db", "-database-name",
                    "schemaspy.db", "schemaspy.database-name"
            },
            descriptionKey = "databaseName"
    )
    private String databaseName;

    @Parameter(
            names = {
                    "-sso","--single-sign-on",
                    "schemaspy.sso", "schemaspy.single-sign-on"
            },
            descriptionKey = "sso"
    )
    private boolean sso = false;

    @Parameter(
            names = {
                    "-u", "--user", "user",
                    "schemaspy.u", "schemaspy.user"},
            descriptionKey = "user"
    )
    private String user;

    @Parameter(
            names = {
                    "-s", "--schema", "schema",
                    "schemaspy.s", "schemaspy.schema"

            },
            descriptionKey = "schema"
    )
    private String schema;

    @Parameter(
            names = {
                    "-cat", "--catalog", "catalog",
                    "schemaspy.cat", "schemaspy.catalog"
            },
            descriptionKey = "catalog"
    )
    private String catalog;

    /* TODO Password handling is more complex, see Config class (prompt for password, fallback to Environment variable, multiple schemas, etc.)
    @Parameter(
            names = {
                    "-p", "--password", "password",
                    "schemaspy.p", "schemaspy.password"
            },
            descriptionKey = "password",
            password = true
    )
    private String password; */

    @Parameter(
            names = {
                    "-dp", "--driverPath", "driverPath",
                    "schemaspy.dp", "schemaspy.driverPath"
            },
            descriptionKey = "driverPath"
    )
    private String driverPath;

    @Parameter(
            names = {
                    "-o", "--outputDirectory", "outputDirectory",
                    "schemaspy.o", "schemaspy.outputDirectory"
            },
            descriptionKey = "outputDirectory"
    )
    private File outputDirectory;

    @Parameter(
            names = {
                    "-port", "--port", "port",
                    "schemaspy.port"
            }
    )
    private Integer port;

    public boolean isHelpRequired() {
        return helpRequired;
    }

    public boolean isDbHelpRequired() {
        return dbHelpRequired;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getSchema() {
        return schema;
    }

    public boolean isSingleSignOn() {
        return sso;
    }

    public String getUser() {
        return user;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Integer getPort() {
        return port;
    }
}
