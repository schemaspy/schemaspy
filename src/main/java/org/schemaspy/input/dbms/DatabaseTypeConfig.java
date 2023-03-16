package org.schemaspy.input.dbms;

import java.util.Properties;

public interface DatabaseTypeConfig {
    String getType();
    Properties getProperties();
}
