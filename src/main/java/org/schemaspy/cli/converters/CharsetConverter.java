package org.schemaspy.cli.converters;

import com.beust.jcommander.IStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

public class CharsetConverter implements IStringConverter<Charset> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Charset convert(String value) {
        try {
            return Charset.forName(value);
        } catch (UnsupportedCharsetException e) {
            LOGGER.warn("Unable to find charset '{}', falling back to UTF-8", value, e);
            return StandardCharsets.UTF_8;
        }
    }
}
