/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2014 John Currier
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
package net.sourceforge.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TableIndex implements Comparable<TableIndex> {
    private final String name;
    private final boolean isUnique;
    private Object id;
    private boolean isPrimary;
    private final List<TableColumn> columns = new ArrayList<TableColumn>();
    private final List<Boolean> columnsAscending = new ArrayList<Boolean>(); // for whether colums are ascending order

    /**
     * @param rs
     * @throws java.sql.SQLException
     */
    public TableIndex(ResultSet rs) throws SQLException {
        name = rs.getString("INDEX_NAME");
        isUnique = !rs.getBoolean("NON_UNIQUE");
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    void addColumn(TableColumn column, String sortOrder) {
        if (column != null) {
            columns.add(column);
            columnsAscending.add(Boolean.valueOf(sortOrder == null || sortOrder.equals("A")));
        }
    }

    /**
     * @return
     */
    public String getType() {
        if (isPrimaryKey())
            return "Primary key";
        if (isUnique())
            return "Must be unique";
        return "Performance";
    }

    /**
     * @return
     */
    public boolean isPrimaryKey() {
        return isPrimary;
    }

    /**
     * @param isPrimaryKey
     */
    public void setIsPrimaryKey(boolean isPrimaryKey) {
        isPrimary = isPrimaryKey;
    }

    /**
     * @return
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * @return
     */
    public String getColumnsAsString() {
        StringBuilder buf = new StringBuilder();

        for (TableColumn column : columns) {
            if (buf.length() > 0)
                buf.append(" + ");
            buf.append(column);
        }
        return buf.toString();
    }

    public String getSortAsString() {
        StringBuilder buf = new StringBuilder();
        Iterator<TableColumn> columnsIter = columns.iterator();
        while (columnsIter.hasNext()) {
            TableColumn column = columnsIter.next();
            if (this.isAscending(column))
                buf.append("<span title='Ascending'>Asc</span>");
            else
                buf.append("<span title='Descending'>Desc</span>");
            if (columnsIter.hasNext())
                buf.append("/");
        }

        return buf.toString();
    }

    public List<TableColumn> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * @param column
     * @return
     */
    public boolean isAscending(TableColumn column) {
        return columnsAscending.get(columns.indexOf(column)).booleanValue();
    }

    /**
     * @param object
     * @return
     */
    @Override
	public int compareTo(TableIndex other) {
        if (isPrimaryKey() && !other.isPrimaryKey())
            return -1;
        if (!isPrimaryKey() && other.isPrimaryKey())
            return 1;

        Object thisId = getId();
        Object otherId = other.getId();
        if (thisId == null || otherId == null)
            return getName().compareToIgnoreCase(other.getName());
        if (thisId instanceof Number)
            return ((Number)thisId).intValue() - ((Number)otherId).intValue();
        return thisId.toString().compareToIgnoreCase(otherId.toString());
    }
}