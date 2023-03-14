package org.schemaspy.cli;

import com.beust.jcommander.IStringConverter;

import java.util.ArrayList;
import java.util.List;

public class SchemasListConverter implements IStringConverter<List<String>> {

    @Override
    public List<String> convert(String value) {
        List<String> schemas = new ArrayList<>();
        for (String name : value.split(",")) {
            if (name.length() > 0)
                schemas.add(name);
        }
        return schemas;
    }
}
