package org.schemaspy.input.dbms;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public interface DbmsConfig {

    boolean isExportedKeysEnabled();
    boolean isNumRowsEnabled();
    boolean isViewsEnabled();
    Pattern getColumnExclusions();
    Pattern getIndirectColumnExclusions();
    Pattern getTableInclusions();
    Pattern getTableExclusions();
    boolean isEvaluateAllEnabled();
    String getSchemaSpec();
    String getDatabaseType();
    Properties getDatabaseTypeProperties();
    List<String> getSchemas();
    int getMaxDbThreads();
    Set<String> getBuiltInDatabaseTypes();
}
