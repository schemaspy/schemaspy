package org.schemaspy.model;

import org.schemaspy.util.naming.Name;

public class PrimaryTableColumn {

    private final Tables tables;
    private final Name name;

    public PrimaryTableColumn(final Tables tables, final Name name) {
        this.tables = tables;
        this.name = name;
    }

    public TableColumn column() {
        final Table primaryTable = this.tables.table(name);
        if (primaryTable != null) {
            return primaryTable.getColumn("ID");
        }
        return null;
    }
}
