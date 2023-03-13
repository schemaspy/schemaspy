package org.schemaspy.input.dbms.config;

import java.util.Properties;

public interface PropertiesResolver {
    Properties getDbProperties(String dbType);
}
