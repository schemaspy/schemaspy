package org.schemaspy.input.dbms;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.schemaspy.util.DbSpecificConfig;

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
    Iterable<Path> getDriverPath();
    List<String> getRemainingArguments();
}
