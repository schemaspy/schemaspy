package org.schemaspy.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import org.schemaspy.Config;
import org.schemaspy.util.DbSpecificConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This class uses {@link JCommander} to parse the SchemaSpy command line arguments represented by {@link CommandLineArguments}.
 */
public class CommandLineArgumentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final JCommander jCommander;

    private final PropertyFileDefaultProvider defaultProvider;

    private static final String[] requiredFields = {"outputDirectory"};

    public CommandLineArgumentParser(PropertyFileDefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
        jCommander = createJCommander();
    }

    private JCommander createJCommander() {
        return JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("java -jar " + Config.getLoadedFromJar())
                .columnSize(120)
                .defaultProvider(defaultProvider)
                .build();
    }

    public CommandLineArguments parse(String... localArgs) {
        CommandLineArguments arguments = new CommandLineArguments();
        jCommander.addObject(arguments);

        jCommander.parse(localArgs);

        if (shouldValidate()) {
            validate(arguments);
        }
        return arguments;
    }

    private boolean shouldValidate() {
        List<ParameterDescription> helpParameters = jCommander.getParameters()
                .stream()
                .filter(ParameterDescription::isHelp)
                .collect(Collectors.toList());
        for(ParameterDescription parameterDescription: helpParameters) {
            if (parameterDescription.isAssigned()) {
                return false;
            }
        }
        return true;
    }

    private void validate(CommandLineArguments arguments) {
        List<String> runtimeRequiredFields = computeRequiredFields(arguments);

        List<String> missingFields = new ArrayList<>();
        Map<String, ParameterDescription> fieldToParameterDescription = jCommander.getParameters()
                .stream().collect(Collectors.toMap(
                        parameterDescription -> parameterDescription.getParameterized().getName(),
                        parameterDescription -> parameterDescription ));
        for (String field : runtimeRequiredFields) {
            ParameterDescription parameterDescription = fieldToParameterDescription.get(field);
            if (valueIsMissing(parameterDescription)) {
                missingFields.add("[" + String.join(" | ", parameterDescription.getParameter().names()) + "]");
            }
        }
        if (!missingFields.isEmpty()) {
            String message = String.join(", ", missingFields);
            throw new ParameterException("The following "
                    + (missingFields.size() == 1 ? "option is required: " : "options are required: ")
                    + message);
        }
    }

    private List<String> computeRequiredFields(CommandLineArguments arguments) {
        List<String> computedRequiredFields = new ArrayList<>(Arrays.asList(requiredFields));
        if (!arguments.isSingleSignOn()) {
            computedRequiredFields.add("user");
        }
        return computedRequiredFields;
    }

    private boolean valueIsMissing(ParameterDescription parameterDescription) {
        Object value = parameterDescription.getParameterized().get(parameterDescription.getObject());
        if (value instanceof String) {
            return ((String)value).isEmpty();
        }
        return Objects.isNull(value);
    }


    /**
     * Prints documentation about the usage of command line arguments to the console.
     * <p>
     */
    //TODO consider extracting dump generation to other class
    public void printUsage() {
        StringBuilder builder = new StringBuilder();

        jCommander.usage(builder);

        builder.append(System.lineSeparator());
        builder.append("Go to http://schemaspy.org for a complete list/description of additional parameters.");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("Sample usage using the default database type (implied -t ora):");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output");
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());

        LOGGER.info("{}", builder);
    }

    /**
     * Prints information of supported database types to the console.
     * <p>
     */
    public void printDatabaseTypesHelp() {
        String schemaspyJarFileName = Config.getLoadedFromJar();

        LOGGER.info("Built-in database types and their required connection parameters:");
        for (String type : Config.getBuiltInDatabaseTypes(schemaspyJarFileName)) {
            new DbSpecificConfig(type).dumpUsage();
        }
        LOGGER.info("You can use your own database types by specifying the filespec of a .properties file with -t.");
        LOGGER.info("Grab one out of {} and modify it to suit your needs.", schemaspyJarFileName);
    }
}
