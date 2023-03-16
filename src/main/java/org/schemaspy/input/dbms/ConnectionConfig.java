package org.schemaspy.input.dbms;

import org.schemaspy.util.DbSpecificConfig;

import java.util.List;
import java.util.Properties;

public interface ConnectionConfig {

    String getDatabaseType();
    Properties getDatabaseTypeProperties();
    String getDatabaseName();
    String getHost();
    Integer getPort();
    String getUser();
    String getPassword();
    String getConnectionProperties();
    DbSpecificConfig getDbSpecificConfig();
    String getDriverPath();
    boolean withLoadSiblings();
    List<String> getRemainingArguments();
}
