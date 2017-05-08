package org.schemaspy.cli;

import com.beust.jcommander.Parameter;

import java.util.Optional;

/**
 * This class only contains the value for the optioanl <code>configFile</code> command line argument.
 * When setting the configFile parameter the user can provide an external properties file
 * that holds values for all other configuration parameter.
 * <p>
 * Example:
 * <p>
 * Command line call:
 * <pre>
 *     java -jar schemaspy.jar -configFile myconfig.properties
 * </pre>
 * <p>
 * Content of myconfig.properties:
 * <pre>
 *     schemaspy.databaseType=mysql
 *     schemaspy.outputDirectory=schemaspy-report
 *     schemaspy.user=MyUser
 *     ...
 * </pre>
 * <p>
 * Schemaspy checks for the presence <code>configFile</code> argument before any other arguments defined in {@link CommandLineArguments} in {@link ApplicationStartListener}.
 */
public final class ConfigFileArgument {

    @Parameter(names = "-configFile")
    private String configFile;

    public Optional<String> getConfigFile() {
        return Optional.ofNullable(configFile);
    }
}
