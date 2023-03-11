package org.schemaspy.output.dot;

public interface DotConfig {
    String getFont();
    int getFontSize();
    boolean isRankDirBugEnabled();
    String getCss();
    String getTemplateDirectory();
    boolean isNumRowsEnabled();
    int getMaxDetailedTables();
}
