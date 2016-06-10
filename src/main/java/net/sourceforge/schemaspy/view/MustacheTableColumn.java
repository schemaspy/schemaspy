package net.sourceforge.schemaspy.view;
import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.LineWriter;
import net.sourceforge.schemaspy.util.Markdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rkasa on 2016-03-23.
 */
public class MustacheTableColumn {

    private TableColumn column;
    private List<MustacheTableColumnRelatives> parents = new ArrayList<>();
    private List<MustacheTableColumnRelatives> children = new ArrayList<>();
    private Set<TableColumn> indexedColumns;
    private String rootPath;

    public MustacheTableColumn(TableColumn tableColumn) {
        this.column = tableColumn;
        prepareRelatives(children, false);
        prepareRelatives(parents, true);
    }

    public MustacheTableColumn(TableColumn tableColumn, Set<TableColumn> indexedColumns, String rootPath) {
        this(tableColumn);
        this.indexedColumns = indexedColumns;
        this.rootPath = rootPath;
    }

    public TableColumn getColumn() {
        return column;
    }

    /**
     * Returns <code>name of css class primaryKey</code> if this column is a primary key
     *
     * @return
     */

    public String getKey() {
        String keyType = "";

        if (column.isPrimary()) {
            keyType = " class='primaryKey' title='Primary Key'";
        } else if (column.isForeignKey()) {
            keyType = " class='foreignKey' title='Foreign Key'";
        } else if (isIndex()) {
            keyType = " class='"+markAsIndexColumn()+"' title='Indexed'";
        }
        return keyType;
    }

    public String getKeyIcon() {
        String keyIcon = "";
        if (column.isPrimary() || column.isForeignKey()) {
            keyIcon = "<i class='icon ion-key iconkey' style='padding-left: 5px;'></i>";
        } else if (isIndex()) {
            keyIcon = "<i class='fa fa-sitemap fa-rotate-120' style='padding-right: 5px;'></i>";
        }

        return  keyIcon;
    }

    public String getNullable() {
        return column.isNullable() ? "√" : "";
    }

    public String getTitleNullable() {
        return column.isNullable() ? "nullable" : "";
    }

    public String getAutoUpdated() {
        return column.isAutoUpdated() ? "√" : "";
    }

    public String getTitleAutoUpdated() {
        return column.isAutoUpdated() ? "Automatically updated by the database" : "";
    }

    private boolean isIndex() {
        if (indexedColumns != null) {
            return indexedColumns.contains(column);
        }
        return false;
    }

    private String markAsIndexColumn() {
        return isIndex() ? "indexedColumn" : "";
    }

    public String getDefaultValue() {
        return String.valueOf(column.getDefaultValue());
    }

    public List<MustacheTableColumnRelatives> getParents() {
        return parents;
    }

    public List<MustacheTableColumnRelatives> getChildren() {
        return children;
    }

    public String getComments() {
        String comments = column.getComments();
        comments = Markdown.toHtml(comments,rootPath);
        return comments;
    }

    private void prepareRelatives(List<MustacheTableColumnRelatives> relatives, boolean dumpParents) {
        Set<TableColumn> columns = dumpParents ? column.getParents() : column.getChildren();

        for (TableColumn column : columns) {

            ForeignKeyConstraint constraint = dumpParents ? column.getChildConstraint(this.column) : column.getParentConstraint(this.column);
            MustacheTableColumnRelatives relative = new MustacheTableColumnRelatives(column, constraint);
//            if (constraint.isImplied())
//                out.writeln("   <tr class='impliedRelationship relative " + evenOdd + "' valign='top'>");
//            else
//                out.writeln("   <tr class='relative " + evenOdd + "' valign='top'>");
//            out.write("    <td class='relatedTable detail' title=\"");
//            out.write(constraint.toString());
//            out.write("\">");
//            if (columnTable.isRemote() && !Config.getInstance().isOneOfMultipleSchemas()) {
//                out.write(columnTable.getContainer());
//                out.write('.');
//                out.write(columnTableName);
//            } else {
//                if (columnTable.isRemote()) {
//                    out.write("<a href='");
//                    out.write(path);
//                    out.write("../../" + urlEncode(columnTable.getContainer()) + "/index.html'>");
//                    out.write(columnTable.getContainer());
//                    out.write("</a>.");
//                }
//                out.write("<a href='");
//                out.write(path);
//                if (columnTable.isRemote()) {
//                    out.write("../../" + urlEncode(columnTable.getContainer()) + "/tables/");
//                }
//                out.write(urlEncode(columnTableName));
//                out.write(".html'>");
//                out.write(columnTableName);
//                out.write("</a>");
//            }
//            out.write("<span class='relatedKey'>.");
//            out.write(column.getName());
//            out.writeln("</span>");
//            out.writeln("    </td>");
//
//            out.write("    <td class='constraint detail'>");
//            out.write(constraint.getName());
//            String ruleText = constraint.getDeleteRuleDescription();
//            if (ruleText.length() > 0)
//            {
//                String ruleAlias = constraint.getDeleteRuleAlias();
//                out.write("<span title='" + ruleText + "'>&nbsp;" + ruleAlias + "</span>");
//            }
//            out.writeln("</td>");
//
//            out.writeln("   </tr>");
            relatives.add(relative);
        }
    }
}

