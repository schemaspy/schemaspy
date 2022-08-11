package org.schemaspy.connection;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Specifies connection properties to use in the format:
 * <code>key1\=value1;key2\=value2</code><br>
 * user (from -u) and password (from -p) will be passed in the
 * connection properties if specified.<p>
 * This is an alternative form of passing connection properties than by file
 */
public class SemicolonSeparated {

    private final String properties;
    private static final String ESCAPED_EQUALS = "\\=";

    /**
     * @param properties string with key\\=value pairs separated by ; of connection properties
     */
    public SemicolonSeparated(String properties) {
        this.properties = properties;
    }

    public Properties setConnectionProperties() {
        Properties result = new Properties();
        StringTokenizer tokenizer = new StringTokenizer(properties, ";");
        while (tokenizer.hasMoreElements()) {
            String pair = tokenizer.nextToken();
            int index = pair.indexOf(ESCAPED_EQUALS);
            if (index != -1) {
                String key = pair.substring(0, index);
                String value = pair.substring(index + ESCAPED_EQUALS.length());
                result.put(key, value);
            }
        }
        return result;
    }
}
