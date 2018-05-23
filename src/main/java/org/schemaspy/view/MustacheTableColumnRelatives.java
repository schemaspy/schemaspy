/*
 * Copyright (C) 2016, 2017 Rafal Kasa
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
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

/**
 * Created by rkasa on 2016-03-24.
 *
 * @author Rafal Kasa
 */
public class MustacheTableColumnRelatives {
    private TableColumn column;
    private Table table;
    private ForeignKeyConstraint constraint;
    private String path;

    public MustacheTableColumnRelatives(ForeignKeyConstraint constraint) {
        this.constraint = constraint;
    }

    public MustacheTableColumnRelatives(TableColumn column, ForeignKeyConstraint constraint) {
        this(constraint);
        this.column = column;
        this.table = column.getTable();
        this.path = table.isRemote() ? ("../../" + table.getContainer() + "/tables/") : "";
    }

    public Table getTable() {
        return table;
    }

    public String getPath() {
        return path;
    }

    public ForeignKeyConstraint getConstraint() {
        return constraint;
    }

    public TableColumn getColumn() {
        return column;
    }
}
