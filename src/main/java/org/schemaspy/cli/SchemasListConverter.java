package org.schemaspy.cli;

import com.beust.jcommander.IStringConverter;

import java.util.HashSet;
import java.util.Set;

public class SchemasListConverter implements IStringConverter<Set<String>> {

    @Override
    public Set<String> convert(String value) {
        Set<String> schemas = new HashSet<>();
        for (String name : value.split(",")) {
            if (name.length() > 0) {
                schemas.add(name);
            }
        }
        return schemas;
    }
}
