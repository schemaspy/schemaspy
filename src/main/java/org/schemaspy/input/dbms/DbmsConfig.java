package org.schemaspy.input.dbms;

import java.util.regex.Pattern;

public interface DbmsConfig {

    boolean isExportedKeysEnabled();
    boolean isNumRowsEnabled();
    boolean isViewsEnabled();
    Pattern getColumnExclusions();
}
