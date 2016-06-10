package net.sourceforge.schemaspy.view;

import net.sourceforge.schemaspy.model.Table;

/**
 * Created by rkasa on 2016-04-01.
 */
public class MustacheTable {
    private Table table;
    private String diagramName;
    private String comments;

    public MustacheTable(Table table, String imageFile) {
        this.table = table;
        diagramName = imageFile;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getDiagramName() {
        return diagramName;
    }

    public void setDiagramName(String diagramName) {
        this.diagramName = diagramName;
    }
}
