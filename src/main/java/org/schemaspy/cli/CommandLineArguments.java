package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

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
                    "-sso","--single-sign-on",
                    "schemaspy.sso", "schemaspy.single-sign-on"
            },
            descriptionKey = "sso"
    )
    private boolean sso = false;

    @Parameter(
            names = {
                    "-o", "--outputDirectory", "outputDirectory",
                    "schemaspy.o", "schemaspy.outputDirectory"
            },
            descriptionKey = "outputDirectory"
    )
    private File outputDirectory;

    @ParametersDelegate
    private DbmsCommandLineArguments dbmsCommandLineArguments = new DbmsCommandLineArguments();

    public boolean isHelpRequired() {
        return helpRequired;
    }

    public boolean isDbHelpRequired() {
        return dbHelpRequired;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isSingleSignOn() {
        return sso;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public DbmsCommandLineArguments getDbmsCommandLineArguments() {
        return dbmsCommandLineArguments;
    }

    public String getDatabaseType() {
        return dbmsCommandLineArguments.getDatabaseType();
    }

    public String getSchema() {
        return dbmsCommandLineArguments.getSchema();
    }

    public String getUser() {
        return dbmsCommandLineArguments.getUser();
    }

    public String getCatalog() {
        return dbmsCommandLineArguments.getCatalog();
    }

    public String getDatabaseName() {
        return dbmsCommandLineArguments.getDatabaseName();
    }

    public Integer getPort() {
        return dbmsCommandLineArguments.getIntPort();
    }
}
