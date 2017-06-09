package org.schemaspy.cli;

import com.beust.jcommander.JCommander;
import org.schemaspy.Config;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class ConfigFileArgumentParser {

    public Optional<String> parseConfigFileArgumentValue(String... args) {
        Objects.requireNonNull(args, "Command line arguments must not be null.");

        JCommander jCommander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("java -jar " + Config.getLoadedFromJar())
                .columnSize(120)
                .build();

        ConfigFileArgument configFileArgument = new ConfigFileArgument();
        jCommander.addObject(configFileArgument);
        jCommander.parse(args);
        return configFileArgument.getConfigFile();
    }
}
