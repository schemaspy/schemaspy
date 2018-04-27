/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.model;

import org.schemaspy.model.xml.TableColumnMeta;

import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 */
public class TableColumn {
    private final Table table;
    private String name;
    private Object id;
    private String typeName;
    private String shortTypeName;
    private Integer type;
    private int length;
    private int decimalDigits;
    private String detailedSize;
    private boolean isNullable;
    private boolean isAutoUpdated;
    private Boolean isUnique;
    private Object defaultValue;
    private String comments;
    private final Map<TableColumn, ForeignKeyConstraint> parents = new HashMap<>();
    private final Map<TableColumn, ForeignKeyConstraint> children = new TreeMap<>(new ColumnComparator());
    private boolean allowImpliedParents = true;
    private boolean allowImpliedChildren = true;
    private boolean isExcluded = false;
    private boolean isAllExcluded = false;

    /**
     * Create a column associated with a table.
     *
     * @param table Table the table that this column belongs to
     */
    public TableColumn(Table table) {
        this.table = table;
    }

    /**
     * A TableColumn that's derived from something other than traditional database metadata
     * (e.g. defined in XML).
     *
     * @param table
     * @param colMeta
     */
    public TableColumn(Table table, TableColumnMeta colMeta) {
        this.table = table;
        name = colMeta.getName();
        id = colMeta.getId();
        typeName = colMeta.getType();
        length = colMeta.getSize();
        decimalDigits = colMeta.getDigits();
        StringBuilder buf = new StringBuilder();
        buf.append(length);
        if (decimalDigits > 0) {
            buf.append(',');
            buf.append(decimalDigits);
        }
        detailedSize = buf.toString();
        isNullable = colMeta.isNullable();
        isAutoUpdated = colMeta.isAutoUpdated();
        defaultValue = colMeta.getDefaultValue();
        comments = colMeta.getComments();
    }



    /**
     * Returns the {@link Table} that this column belongs to.
     *
     * @return
     */
    public Table getTable() {
        return table;
    }

    /**
     * Returns the column's name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the ID of the column or <code>null</code> if the database doesn't support the concept.
     *
     * @return
     */
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    /**
     * Type of the column.
     * See {@link DatabaseMetaData#getColumns(String, String, String, String)}'s <code>DATA_TYPE</code>.
     * @return integer from java.sql.Types or <code>null</code> if not set
     */
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
    	this.type = type;
    }
    
    /**
     * Type of the column.
     * See {@link DatabaseMetaData#getColumns(String, String, String, String)}'s <code>TYPE_NAME</code>.
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Normally only used for "special" types such as enums.
     *
     * @param type
     */
    public void setTypeName(String type) {
        this.typeName = type;
    }

    /**
     * Abbreviated form of {@link #getTypeName()}
     *
     * @return
     */
    public String getShortTypeName() {
        return shortTypeName == null ? typeName : shortTypeName;
    }

    /**
     * Abbreviated form of {@link #setTypeName(String)}
     *
     * @param shortType
     */
    public void setShortType(String shortType) {
        this.shortTypeName = shortType;
    }

    /**
     * Length of the column.
     * See {@link DatabaseMetaData#getColumns(String, String, String, String)}'s <code>BUFFER_LENGTH</code>,
     * or if that's <code>null</code>, <code>COLUMN_SIZE</code>.
     * @return
     */
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Decimal digits of the column.
     * See {@link DatabaseMetaData#getColumns(String, String, String, String)}'s <code>DECIMAL_DIGITS</code>.
     *
     * @return
     */
    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    /**
     * String representation of length with optional decimal digits (if decimal digits &gt; 0).
     *
     * @return
     */
    public String getDetailedSize() {
        return detailedSize;
    }

    public void setDetailedSize(String detailedSize) {
        this.detailedSize = detailedSize;
    }

    /**
     * Returns <code>true</code> if null values are allowed
     *
     * @return
     */
    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    /**
     * See {@link java.sql.ResultSetMetaData#isAutoIncrement(int)}
     *
     * @return
     */
    public boolean isAutoUpdated() {
        return isAutoUpdated;
    }

    /**
     * setIsAutoUpdated
     *
     * @param isAutoUpdated boolean
     */
    public void setIsAutoUpdated(boolean isAutoUpdated) {
        this.isAutoUpdated = isAutoUpdated;
    }

    /**
     * Returns <code>true</code> if this column can only contain unique values
     *
     * @return
     */
    public boolean isUnique() {
        if (isUnique == null) {
            // see if there's a unique index on this column by itself
            for (TableIndex index : table.getIndexes()) {
                if (index.isUnique()) {
                    List<TableColumn> indexColumns = index.getColumns();
                    if (indexColumns.size() == 1 && indexColumns.contains(this)) {
                        isUnique = true;
                        break;
                    }
                }
            }

            if (isUnique == null) {
                // if it's a single PK column then it's unique
                isUnique = table.getPrimaryColumns().size() == 1 && isPrimary();
            }
        }

        return isUnique;
    }

    /**
     * Returns <code>true</code> if this column is a primary key
     *
     * @return
     */
    public boolean isPrimary() {
        if (table.getPrimaryColumns() != null) {
            return table.getPrimaryColumns().contains(this);
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this column points to another table's primary key.
     *
     * @return
     */
    public boolean isForeignKey() {
        return !parents.isEmpty();
    }

    /**
     * Returns the value that the database uses for this column if one isn't provided.
     *
     * @return
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return Comments associated with this column, or <code>null</code> if none.
     */
    public String getComments() {
        return comments;
    }

    /**
     * See {@link #getComments()}
     * @param comments
     */
    public void setComments(String comments) {
        this.comments = (comments == null || comments.trim().length() == 0) ? null : comments.trim();
    }

    /**
     * Returns <code>true</code> if this column is to be excluded from relationship diagrams.
     * Unless {@link #isAllExcluded()} is true this column will be included in the detailed
     * diagrams of the containing table.
     *
     * <p>This is typically an attempt to reduce clutter that can be introduced when many tables
     * reference a given column.
     *
     * @return
     */
    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(boolean excluded) {
        isExcluded = excluded;
    }

    /**
     * Returns <code>true</code> if this column is to be excluded from all relationships in
     * relationship diagrams.  This includes the detailed diagrams of the containing table.
     *
     * <p>This is typically an attempt to reduce clutter that can be introduced when many tables
     * reference a given column.
     *
     * @return
     */
    public boolean isAllExcluded() {
        return isAllExcluded;
    }

    public void setAllExcluded(boolean allExcluded) {
        isAllExcluded = allExcluded;
    }

    /**
     * Add a parent column (PK) to this column (FK) via the associated constraint
     *
     * @param parent
     * @param constraint
     */
    public void addParent(TableColumn parent, ForeignKeyConstraint constraint) {
        parents.put(parent, constraint);
        table.addedParent();
    }

    /**
     * Remove the specified parent column from this column
     *
     * @param parent
     */
    public void removeParent(TableColumn parent) {
        parents.remove(parent);
    }

    /**
     * Disassociate all parents from this column
     */
    public void unlinkParents() {
        for (TableColumn parent : parents.keySet()) {
            parent.removeChild(this);
        }
        parents.clear();
    }

    /**
     * Returns the {@link Set} of all {@link TableColumn parents} associated with this column
     *
     * @return
     */
    public Set<TableColumn> getParents() {
        return parents.keySet();
    }

    /**
     * Returns the constraint that connects this column to the specified column (this 'child' column to specified 'parent' column)
     */
    public ForeignKeyConstraint getParentConstraint(TableColumn parent) {
        return parents.get(parent);
    }

    /**
     * Removes a parent constraint and returns it, or null if there are no parent constraints
     *
     * @return the removed {@link ForeignKeyConstraint}
     */
    public ForeignKeyConstraint removeAParentFKConstraint() {
        for (TableColumn relatedColumn : parents.keySet()) {
            ForeignKeyConstraint constraint = parents.remove(relatedColumn);
            relatedColumn.removeChild(this);
            return constraint;
        }

        return null;
    }

    /**
     * Remove one child {@link ForeignKeyConstraint} that points to this column.
     *
     * @return the removed constraint, or <code>null</code> if none were available to be removed
     */
    public ForeignKeyConstraint removeAChildFKConstraint() {
        for (TableColumn relatedColumn : children.keySet()) {
            ForeignKeyConstraint constraint = children.remove(relatedColumn);
            relatedColumn.removeParent(this);
            return constraint;
        }

        return null;
    }

    /**
     * Add a child column (FK) to this column (PK) via the associated constraint
     *
     * @param child
     * @param constraint
     */
    public void addChild(TableColumn child, ForeignKeyConstraint constraint) {
        children.put(child, constraint);
        table.addedChild();
    }

    /**
     * Remove the specified child column from this column
     *
     * @param child
     */
    public void removeChild(TableColumn child) {
        children.remove(child);
    }

    /**
     * Disassociate all children from this column
     */
    public void unlinkChildren() {
        for (TableColumn child : children.keySet())
            child.removeParent(this);
        children.clear();
    }

    /**
     * Returns <code>Set</code> of <code>TableColumn</code>s that have a real (or implied) foreign key that
     * references this <code>TableColumn</code>.
     * @return Set
     */
    public Set<TableColumn> getChildren() {
        return children.keySet();
    }

    /**
     * returns the constraint that connects the specified column to this column
     * (specified 'child' to this 'parent' column)
     */
    public ForeignKeyConstraint getChildConstraint(TableColumn child) {
        return children.get(child);
    }

    /**
     * Returns <code>true</code> if tableName.columnName matches the supplied
     * regular expression.
     *
     * @param regex
     * @return
     */
    public boolean matches(Pattern regex) {
        return regex.matcher(getTable().getName() + '.' + getName()).matches();
    }

    /**
     * Update the state of this column with the supplied {@link TableColumnMeta}.
     * Intended to be used with instances created by {@link #TableColumn(Table, TableColumnMeta)}.
     *
     * @param colMeta
     */
    public void update(TableColumnMeta colMeta) {
        String newComments = colMeta.getComments();
        if (newComments != null)
            setComments(newComments);

        if (!isPrimary() && colMeta.isPrimary()) {
            table.setPrimaryColumn(this);
        }

        allowImpliedParents  = !colMeta.isImpliedParentsDisabled();
        allowImpliedChildren = !colMeta.isImpliedChildrenDisabled();
        isExcluded |= colMeta.isExcluded();
        isAllExcluded |= colMeta.isAllExcluded();
    }

    /**
     * Returns the name of this column.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Two {@link TableColumn}s are considered equal if their tables and names match.
     */
    private class ColumnComparator implements Comparator<TableColumn> {
        public int compare(TableColumn column1, TableColumn column2) {
            int rc = column1.getTable().compareTo(column2.getTable());
            if (rc == 0)
                rc = column1.getName().compareToIgnoreCase(column2.getName());
            return rc;
        }
    }

    /**
     * Returns <code>true</code> if this column is permitted to be an implied FK
     * (based on name/type/size matches to PKs).
     *
     * @return
     */
    public boolean allowsImpliedParents() {
        return allowImpliedParents;
    }

    /**
     * Returns <code>true</code> if this column is permitted to be a PK to an implied FK
     * (based on name/type/size matches to PKs).
     *
     * @return
     */
    public boolean allowsImpliedChildren() {
        return allowImpliedChildren;
    }
}