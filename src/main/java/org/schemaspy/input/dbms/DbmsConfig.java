package org.schemaspy.input.dbms;

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
}
