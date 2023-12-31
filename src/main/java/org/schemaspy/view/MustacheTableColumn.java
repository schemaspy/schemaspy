/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.view;

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.TableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rkasa on 2016-03-23.
 *
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class MustacheTableColumn {

    private TableColumn column;
    private List<MustacheTableColumnRelatives> parents = new ArrayList<>();
    private List<MustacheTableColumnRelatives> children = new ArrayList<>();
    private boolean indexColumn;

    public MustacheTableColumn(TableColumn tableColumn) {
        this.column = tableColumn;
        prepareRelatives(children, false);
        prepareRelatives(parents, true);
    }

    public MustacheTableColumn(TableColumn tableColumn, boolean indexColumn) {
        this(tableColumn);
        this.indexColumn = indexColumn;
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
        if (column.isPrimary()) {
            return " class='primaryKey' title='Primary Key'";
        } else if (column.isForeignKey()) {
            return  " class='foreignKey' title='Foreign Key'";
        } else if (indexColumn) {
            return " class='"+markAsIndexColumn()+"' title='Indexed'";
        } else {
            return "";
        }
    }

    public String getKeyTitle() {
        if (column.isPrimary()) {
            return "Primary Key";
        } else if (column.isForeignKey()) {
            return "Foreign Key";
        } else if (indexColumn) {
            return "Indexed";
        } else {
            return "";
        }
    }

    public String getKeyClass() {
        if (column.isPrimary()) {
            return "primaryKey";
        } else if (column.isForeignKey()) {
            return "foreignKey";
        } else if (indexColumn) {
            return "indexedColumn";
        } else {
            return "";
        }
    }

    public String getKeyIcon() {
        if (column.isPrimary() || column.isForeignKey()) {
            return "<i class='icon ion-key iconkey' style='padding-left: 5px;'></i>";
        } else if (indexColumn) {
            return  "<i class='fa fa-sitemap fa-rotate-120' style='padding-right: 5px;'></i>";
        } else {
            return "";
        }
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

    private String markAsIndexColumn() {
        return indexColumn ? "indexedColumn" : "";
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
        return column.getComments();
    }

    private void prepareRelatives(List<MustacheTableColumnRelatives> relatives, boolean dumpParents) {
        Set<TableColumn> relativeColumns = dumpParents ? column.getParents() : column.getChildren();

        for (TableColumn relativeColumn : relativeColumns) {

            ForeignKeyConstraint constraint = dumpParents ? relativeColumn.getChildConstraint(this.column) : relativeColumn.getParentConstraint(this.column);
            MustacheTableColumnRelatives relative = new MustacheTableColumnRelatives(relativeColumn, constraint);

            relatives.add(relative);
        }
    }
}

