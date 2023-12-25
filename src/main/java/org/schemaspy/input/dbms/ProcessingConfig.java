package org.schemaspy.input.dbms;

import java.util.Properties;
import java.util.regex.Pattern;

public interface ProcessingConfig {

    String getDatabaseType();
    Properties getDatabaseTypeProperties();
    boolean isExportedKeysEnabled();
    boolean isNumRowsEnabled();
    boolean isViewsEnabled();
    Pattern getColumnExclusions();
    Pattern getIndirectColumnExclusions();
    Pattern getTableInclusions();
    Pattern getTableExclusions();
    int getMaxDbThreads();
    boolean includeRoutineDefinition();
}
