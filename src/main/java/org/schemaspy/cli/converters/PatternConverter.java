package org.schemaspy.cli.converters;

import com.beust.jcommander.IStringConverter;

import java.util.regex.Pattern;

public class PatternConverter implements IStringConverter<Pattern>{
    @Override
    public Pattern convert(String value) {
        return Pattern.compile(value);
    }
}
