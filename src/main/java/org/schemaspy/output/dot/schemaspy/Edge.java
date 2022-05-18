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
package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.dot.schemaspy.graph.Element;

/**
 * Represents Graphvis dot's concept of an edge.  That is, a connector between two nodes.
 *
 * @author John Currier
 * @author Nils Petzaell
 */
public class Edge implements Comparable<Edge>, Element {
    private final TableColumn parentColumn;
    private final Table parentTable;
    private final TableColumn childColumn;
    private final Table childTable;
    private final boolean implied;
    private String parentPort;
    private String childPort;

    /**
     * Create an edge that logically connects a child column to a parent column.
     *
     * @param parentColumn TableColumn
     * @param childColumn TableColumn
     * @param implied boolean
     */
    public Edge(TableColumn parentColumn, TableColumn childColumn, boolean implied) {
        this.parentColumn = parentColumn;
        this.childColumn = childColumn;
        this.implied = implied;
        parentPort = parentColumn.getName();
        parentTable = parentColumn.getTable();
        childPort = childColumn.getName();
        childTable = childColumn.getTable();
    }

    public boolean isImplied() {
        return implied;
    }

    /**
     * Connects the edge to the specified table's type column,
     * should the edge logically "point to" it.
     *
     * @param possibleParent The table to consider.
     */
    public void connectToDetailsLogically(final Table possibleParent) {
        if (possibleParent.equals(parentTable)) {
            connectToParentDetails();
        }
    }

    /**
     * By default a parent edge connects to the column name...this lets you
     * connect it the parent's type column instead (e.g. for detailed parents)
     *
     * Yes, I need to find a more appropriate name/metaphor for this method....
     */
    private void connectToParentDetails() {
        parentPort = parentColumn.getName() + ".type";
    }

    public void connectToParentTitle() {
        parentPort = "elipses";
    }

    public void connectToChildTitle() {
        childPort = "elipses";
    }

    @Override
    public String value() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder edge = new StringBuilder();
        edge.append("  \"");
        if (childTable.isRemote()) {
            edge.append(childTable.getContainer());
            edge.append('.');
        }
        edge.append(childTable.getName());
        edge.append("\":\"");
        edge.append(childPort);
        edge.append("\":");
        edge.append("w -> \"");
        if (parentTable.isRemote()) {
            edge.append(parentTable.getContainer());
            edge.append('.');
        }
        edge.append(parentTable.getName());
        edge.append("\":\"");
        edge.append(parentPort);
        edge.append("\":");
        edge.append("e ");

        // if enabled makes the diagram unreadable
        // have to figure out how to render these details in a readable manner
        final boolean fullErNotation = false;

        // Thanks to Dan Zingaro for figuring out how to correctly annotate
        // these relationships
        if (fullErNotation) {
            // PK end of connector
            edge.append("[arrowhead=");
            if (childColumn.isNullable())
                edge.append("odottee"); // zero or one parents
            else
                edge.append("teetee");  // one parent
            edge.append(" dir=both");
        } else {
            // PK end of connector
            edge.append("[arrowhead=none");
            edge.append(" dir=back");
        }

        // FK end of connector
        edge.append(" arrowtail=");
        if (childColumn.isUnique())
            edge.append("teeodot"); // zero or one children
        else
            edge.append("crowodot");// zero or more children

        if (implied)
            edge.append(" style=dashed");
        edge.append("];");

        return edge.toString();
    }

    public int compareTo(Edge other) {
        int rc = childTable.compareTo(other.childTable);
        if (rc == 0)
            rc = childColumn.getName().compareToIgnoreCase(other.childColumn.getName());
        if (rc == 0)
            rc = parentTable.compareTo(other.parentTable);
        if (rc == 0)
            rc = parentColumn.getName().compareToIgnoreCase(other.parentColumn.getName());
        if (rc == 0 && implied != other.implied)
            rc = implied ? 1 : -1;
        return rc;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Edge))
            return false;
        return compareTo((Edge)other) == 0;
    }

    @Override
    public int hashCode() {
        int p = parentTable == null ? 0 : parentTable.getName().hashCode();
        int c = childTable == null ? 0 : childTable.getName().hashCode();
        return (p << 16) & c;
    }

    public TableColumn getParentColumn() {
        return parentColumn;
    }

    public Table getParentTable() {
        return parentTable;
    }

    public TableColumn getChildColumn() {
        return childColumn;
    }

    public Table getChildTable() {
        return childTable;
    }
}
