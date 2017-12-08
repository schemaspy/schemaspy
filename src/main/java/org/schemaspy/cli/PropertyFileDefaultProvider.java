package org.schemaspy.cli;

import com.beust.jcommander.IDefaultProvider;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link IDefaultProvider} that provides values reading from a {@link Properties} file.
 *
 * TODO
 * JCommander already provides a com.beust.jcommander.defaultprovider.PropertyFileDefaultProvider.
 * But it always reports "cannot find file on classpath" although it exists. Maybe open an issue at the JCommander project?
 */
public class PropertyFileDefaultProvider implements IDefaultProvider {

    private static final Logger LOGGER = Logger.getLogger(PropertyFileDefaultProvider.class.getName());

    private final Properties properties;

    public PropertyFileDefaultProvider(String propertiesFilename) {
        Objects.requireNonNull(propertiesFilename);
        properties = loadProperties(propertiesFilename);
    }

    private static Properties loadProperties(String path) {
        try (Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-8")){
            Properties properties = new Properties();
            String contents = FileCopyUtils.copyToString(reader);
            // Replace backslashes with double backslashes to escape windows path separator.
            // Example input: schemaspy.o=C:\tools\schemaspy\output
            properties.load(new StringReader(contents.replace("\\", "\\\\")));
            return properties;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File not found: " + path, e);
            throw new IllegalArgumentException("Could not find or load properties file: " + path, e);
        }
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        return properties.getProperty(optionName);
    }
}
