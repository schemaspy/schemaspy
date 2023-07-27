package org.schemaspy.cli;

import org.apache.commons.io.IOUtils;
import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Banner {

    private final String resourcePath;
    private final Map<String,String> propertyValues;

    public Banner(String resourcePath, Map<String, String> propertyValues) {
        this.resourcePath = resourcePath;
        this.propertyValues = propertyValues;
    }

    public String banner() {
        try (InputStream inputStream = this.getClass().getResourceAsStream(resourcePath)) {
            String banner = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            for(Map.Entry<String,String> propertyValue : propertyValues.entrySet()) {
                banner = banner.replace(propertyValue.getKey(), propertyValue.getValue());
            }
            return banner;
        } catch (IOException e) {
            throw new RuntimeIOException("Unable to read banner from " + resourcePath, e);
        }
    }
}
