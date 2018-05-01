package org.schemaspy.cli.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConverter implements IStringConverter<Properties> {
    @Override
    public Properties convert(String value) {
        Properties properties = null;
        //TODO remove once -connProp is active
        if (value.contains("\\=")) {
            return properties;
        }
        try (InputStream inputStream = new FileInputStream(value)) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ParameterException("Failed to process '"+value+"' as properties file", e);
        }
        return properties;
    }
}
