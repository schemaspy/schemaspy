/*
 * Copyright (C) 2004 - 2011 John Currier
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.view;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple ugly hack that provides details of what was written.
 *
 * @author John Currier
 */
public class WriteStats {
    private int numTables;
    private int numViews;
    private final Set<TableColumn> excludedColumns;

    public WriteStats(Collection<Table> tables) {
        excludedColumns = new HashSet<TableColumn>();

        for (Table table : tables) {
            for (TableColumn column : table.getColumns()) {
                if (column.isExcluded()) {
                    excludedColumns.add(column);
                }
            }
        }
    }

    public WriteStats(WriteStats stats) {
        excludedColumns = stats.excludedColumns;
    }

    public void wroteTable(Table table) {
        if (table.isView())
            ++numViews;
        else
            ++numTables;
    }

    public int getNumTablesWritten() {
        return numTables;
    }

    public int getNumViewsWritten() {
        return numViews;
    }

    public Set<TableColumn> getExcludedColumns() {
        return excludedColumns;
    }
}