package org.schemaspy.cli.converters;

import com.beust.jcommander.IStringConverter;
import org.schemaspy.view.DefaultSqlFormatter;
import org.schemaspy.view.SqlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class SqlFormatterConverter implements IStringConverter<SqlFormatter> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public SqlFormatter convert(String value) {
        Class<?> sqlFormatterClass = null;
        try {
            sqlFormatterClass = Class.forName(value);
            if (SqlFormatter.class.isAssignableFrom(sqlFormatterClass)) {
                return (SqlFormatter) sqlFormatterClass.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.warn("Failed to find/create SqlFormatter '{}', falling back to DefaultSqlFormatter", value, e);
        }
        return new DefaultSqlFormatter();
    }
}
