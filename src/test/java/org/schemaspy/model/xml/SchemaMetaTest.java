package org.schemaspy.model.xml;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaMetaTest {

    @Rule
    public LoggingRule LOGGING = new LoggingRule();

    @Test
    @Logger(TableMeta.class)
    public void deprecationWarningRemarksInTable() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInTable.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).contains("deprecated");
    }

    @Test
    @Logger(TableColumnMeta.class)
    public void deprecationWarningRemarksInColumn() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInColumn.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).contains("deprecated");
    }

    @Test
    @Logger(TableMeta.class)
    public void okCommentsInTable() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInTable.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).doesNotContain("deprecated");
    }

    @Test
    @Logger(TableColumnMeta.class)
    public void okCommentsInColumn() {
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInColumn.xml", "dbname", "schemaName");
        assertThat(LOGGING.getLog()).doesNotContain("deprecated");
    }

}
