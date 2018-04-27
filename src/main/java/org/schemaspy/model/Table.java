/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Nils Petzaell
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
import org.schemaspy.model.xml.TableMeta;
import org.schemaspy.util.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * A <code>Table</code> is one of the basic building blocks of SchemaSpy
 * that knows everything about the database table's metadata.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class Table implements Comparable<Table> {
    private final String catalog;
    private final String schema;
    private final String name;
    private final String fullName;
    private final String container;
    protected CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
    private final List<TableColumn> primaryKeys = new ArrayList<>();
    private final CaseInsensitiveMap<ForeignKeyConstraint> foreignKeys = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<TableIndex> indexes = new CaseInsensitiveMap<>();
    private Object id;
    private final Map<String, String> checkConstraints = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private long numRows;
    protected final Database db;
    private String comments;
    private int maxChildren;
    private int maxParents;

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Construct a table that knows everything about the database table's metadata
     *
     * @param db
     * @param catalog
     * @param schema
     * @param name
     * @param comments
     */
    public Table(Database db, String catalog, String schema, String name, String comments) {
        this.db = db;
        this.catalog = catalog;
        this.schema = schema;
        this.container = schema != null ? schema : catalog != null ? catalog : db.getName();
        this.name = name;
        this.fullName = getFullName(db.getName(), catalog, schema, name);
        LOGGER.debug("Creating {} {}", getClass().getSimpleName(), fullName);

        setComments(comments);
    }

    /**
     * Get the foreign keys associated with this table
     *
     * @return
     */
    public Collection<ForeignKeyConstraint> getForeignKeys() {
        return Collections.unmodifiableCollection(foreignKeys.values());
    }

    /**
     * Get the foreign keys associated with this table
     *
     * @return
     */
    public CaseInsensitiveMap<ForeignKeyConstraint> getForeignKeysMap() {
        return foreignKeys;
    }

    /**
     * Add a check constraint to the table
     * (no real details, just name and textual representation)
     *
     * @param constraintName
     * @param text
     */
    public void addCheckConstraint(String constraintName, String text) {
        checkConstraints.put(constraintName, text);
    }

    /**
     * @param primaryColumn
     */
    public void setPrimaryColumn(TableColumn primaryColumn) {
        primaryKeys.add(primaryColumn);
    }

    /**
     * Add a column that's defined in xml metadata.
     * Assumes that a column named colMeta.getName() doesn't already exist in <code>columns</code>.
     *
     * @param colMeta
     * @return
     */
    protected TableColumn addColumn(TableColumnMeta colMeta) {
        TableColumn column = new TableColumn(this, colMeta);

        columns.put(column.getName(), column);

        return column;
    }

    /**
     * @param indexName
     * @return
     */
    public TableIndex getIndex(String indexName) {
        return indexes.get(indexName);
    }

    public CaseInsensitiveMap<TableIndex> getIndexesMap() {
        return indexes;
    }

    /**
     * Returns the catalog that the table belongs to
     *
     * @return
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Returns the schema that the table belongs to
     *
     * @return
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the logical 'container' that the table
     * lives in.  Basically it's the first non-<code>null</code>
     * item out of <code>schema</code>, <code>catalog</code>
     * and <code>database</code>.
     *
     * @return
     */
    public String getContainer() {
        return container;
    }

    /**
     * Returns the name of the table
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fully-qualified name of this table
     *
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the fully-qualified name of a table
     *
     * @return
     */
    public static String getFullName(String db, String catalog, String schema, String table) {
        return (catalog == null && schema == null ? db + '.' : "") +
                (catalog == null ? "" : catalog + '.') +
                (schema == null ? "" : schema + '.') + table;
    }

    /**
     * Object IDs are useful for tables such as DB/2 that many times
     * give error messages based on object ID and not name
     *
     * @param id
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * @return
     * @see #setId(Object)
     */
    public Object getId() {
        return id;
    }

    /**
     * Returns the check constraints associated with this table
     *
     * @return
     */
    public Map<String, String> getCheckConstraints() {
        return checkConstraints;
    }

    /**
     * Returns the indexes that are applied to this table
     *
     * @return
     */
    public Set<TableIndex> getIndexes() {
        return new HashSet<>(indexes.values());
    }

    /**
     * Returns a collection of table columns that have been identified as "primary"
     *
     * @return
     */
    public List<TableColumn> getPrimaryColumns() {
        return primaryKeys;
    }

    /**
     * @return Comments associated with this table, or <code>null</code> if none.
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the comments that are associated with this table
     *
     * @param comments
     */
    public void setComments(String comments) {
        String cmts = (comments == null || comments.trim().length() == 0) ? null : comments.trim();

        // MySQL's InnoDB engine does some insane crap of storing erroneous details in
        // with table comments.  Here I attempt to strip the "crap" out without impacting
        // other databases.  Ideally this should happen in selectColumnCommentsSql (and
        // therefore isolate it to MySQL), but it's a bit too complex to do cleanly.
        if (cmts != null) {
            int crapIndex = cmts.indexOf("; InnoDB free: ");
            if (crapIndex == -1)
                crapIndex = cmts.startsWith("InnoDB free: ") ? 0 : -1;
            if (crapIndex != -1) {
                cmts = cmts.substring(0, crapIndex).trim();
                cmts = cmts.length() == 0 ? null : cmts;
            }
        }

        this.comments = cmts;
    }

    /**
     * Returns the {@link TableColumn} with the given name, or <code>null</code>
     * if it doesn't exist
     *
     * @param columnName
     * @return
     */
    public TableColumn getColumn(String columnName) {
        return columns.get(columnName);
    }

    /**
     * Returns <code>List</code> of <code>TableColumn</code>s in ascending column number order.
     *
     * @return
     */
    public List<TableColumn> getColumns() {
        Set<TableColumn> sorted = new TreeSet<>(new ByColumnIdComparator());
        sorted.addAll(columns.values());
        return new ArrayList<>(sorted);
    }

    public void setColumns(CaseInsensitiveMap<TableColumn> columns) {
        this.columns = columns;
    }

    /**
     * Returns <code>CaseInsensitiveMap</code> of <code>TableColumn</code>s.
     *
     * @return
     */
    public CaseInsensitiveMap<TableColumn> getColumnsMap() {
        return columns;
    }

    /**
     * Returns <code>true</code> if this table references no other tables..<p/>
     * Used in dependency analysis.
     *
     * @return
     */
    public boolean isRoot() {
        for (TableColumn column : columns.values()) {
            if (column.isForeignKey()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if this table is referenced by no other tables.<p/>
     * Used in dependency analysis.
     *
     * @return
     */
    public boolean isLeaf() {
        for (TableColumn column : columns.values()) {
            if (!column.getChildren().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the maximum number of parents that this table has had before
     * any had been removed during dependency analysis
     *
     * @return
     */
    public int getMaxParents() {
        return maxParents;
    }

    /**
     * Notification that's called to indicate that a parent has been added to
     * this table
     */
    public void addedParent() {
        maxParents++;
    }

    /**
     * "Unlink" all of the parent tables from this table
     */
    public void unlinkParents() {
        for (TableColumn column : columns.values()) {
            column.unlinkParents();
        }
    }

    /**
     * Returns the maximum number of children that this table has had before
     * any had been removed during dependency analysis
     *
     * @return
     */
    public int getMaxChildren() {
        return maxChildren;
    }

    /**
     * Notification that's called to indicate that a child has been added to
     * this table
     */
    public void addedChild() {
        maxChildren++;
    }

    /**
     * "Unlink" all of the child tables from this table
     */
    public void unlinkChildren() {
        for (TableColumn column : columns.values()) {
            column.unlinkChildren();
        }
    }

    /**
     * Remove a single self referencing constraint if one exists.
     *
     * @return
     */
    public ForeignKeyConstraint removeSelfReferencingConstraint() {
        return remove(getSelfReferencingConstraint());
    }

    /**
     * Remove the specified {@link ForeignKeyConstraint} from this table.<p>
     * <p>
     * This is a more drastic removal solution that was proposed by Remke Rutgers
     *
     * @param constraint
     */
    private ForeignKeyConstraint remove(ForeignKeyConstraint constraint) {
        if (constraint != null) {
            for (int i = 0; i < constraint.getChildColumns().size(); i++) {
                TableColumn childColumn = constraint.getChildColumns().get(i);
                TableColumn parentColumn = constraint.getParentColumns().get(i);
                childColumn.removeParent(parentColumn);
                parentColumn.removeChild(childColumn);
            }
        }
        return constraint;
    }

    /**
     * Return a self referencing constraint if one exists
     *
     * @return
     */
    private ForeignKeyConstraint getSelfReferencingConstraint() {
        for (TableColumn column : columns.values()) {
            for (TableColumn parentColumn : column.getParents()) {
                if (compareTo(parentColumn.getTable()) == 0) {
                    return column.getParentConstraint(parentColumn);
                }
            }
        }
        return null;
    }

    /**
     * Remove any non-real foreign keys
     *
     * @return
     */
    public List<ForeignKeyConstraint> removeNonRealForeignKeys() {
        List<ForeignKeyConstraint> nonReals = new ArrayList<>();

        for (TableColumn column : columns.values()) {
            for (TableColumn parentColumn : column.getParents()) {
                ForeignKeyConstraint constraint = column.getParentConstraint(parentColumn);
                if (constraint != null && !constraint.isReal()) {
                    nonReals.add(constraint);
                }
            }
        }

        // remove constraints outside of above loop to prevent
        // concurrent modification exceptions while iterating
        for (ForeignKeyConstraint constraint : nonReals) {
            remove(constraint);
        }

        return nonReals;
    }

    /**
     * Returns the number of tables that reference this table
     *
     * @return
     */
    public int getNumChildren() {
        int numChildren = 0;

        for (TableColumn column : columns.values()) {
            numChildren += column.getChildren().size();
        }

        return numChildren;
    }

    /**
     * Returns the number of non-implied children
     *
     * @return
     */
    public int getNumNonImpliedChildren() {
        int numChildren = 0;

        for (TableColumn column : columns.values()) {
            for (TableColumn childColumn : column.getChildren()) {
                if (!column.getChildConstraint(childColumn).isImplied())
                    ++numChildren;
            }
        }

        return numChildren;
    }

    /**
     * Returns the number of tables that are referenced by this table
     *
     * @return
     */
    public int getNumParents() {
        int numParents = 0;

        for (TableColumn column : columns.values()) {
            numParents += column.getParents().size();
        }

        return numParents;
    }

    /**
     * Returns the number of non-implied parents
     *
     * @return
     */
    public int getNumNonImpliedParents() {
        int numParents = 0;

        for (TableColumn column : columns.values()) {
            for (TableColumn parentColumn : column.getParents()) {
                if (!column.getParentConstraint(parentColumn).isImplied())
                    ++numParents;
            }
        }

        return numParents;
    }

    /**
     * Remove one foreign key constraint.
     * <p>
     * <p/>Used during dependency analysis phase.
     *
     * @return
     */
    public ForeignKeyConstraint removeAForeignKeyConstraint() {
        final List<TableColumn> columns = getColumns();
        int numParents = 0;
        int numChildren = 0;
        // remove either a child or parent, choosing which based on which has the
        // least number of foreign key associations (when either gets to zero then
        // the table can be pruned)
        for (TableColumn column : columns) {
            numParents += column.getParents().size();
            numChildren += column.getChildren().size();
        }

        for (TableColumn column : columns) {
            ForeignKeyConstraint constraint;
            if (numParents <= numChildren)
                constraint = column.removeAParentFKConstraint();
            else
                constraint = column.removeAChildFKConstraint();
            if (constraint != null)
                return constraint;
        }

        return null;
    }

    /**
     * Returns <code>true</code> if this table is logical (not physical), <code>false</code> otherwise
     *
     * @return
     */
    public boolean isLogical() {
        return false;
    }

    /**
     * Returns <code>true</code> if this is a view, <code>false</code> otherwise
     *
     * @return
     */
    public boolean isView() {
        return false;
    }


    /**
     * Returns name of table type <code>View</code> if this is a view, <code>Table</code> otherwise
     *
     * @return
     */
    public String getType() {
        return isView() ? "View" : "Table";
    }

    /**
     * Returns <code>true</code> if this table is remote (in another schema), <code>false</code> otherwise
     *
     * @return
     */
    public boolean isRemote() {
        return false;
    }

    /**
     * If this is a view it returns the SQL used to create the view (if it's available).
     * <code>null</code> if it's not a view or the SQL isn't available.
     *
     * @return
     * @see #isView()
     */
    public String getViewDefinition() {
        return null;
    }

    /**
     * Returns the number of rows contained in this table, or -1 if unable to determine
     * the number of rows.
     *
     * @return
     */
    public long getNumRows() {
        return numRows;
    }

    /**
     * Explicitly set the number of rows in this table
     *
     * @param numRows
     */
    public void setNumRows(long numRows) {
        this.numRows = numRows;
    }

    /**
     * Update the table with the specified XML-derived metadata
     *
     * @param tableMeta
     */
    public void update(TableMeta tableMeta) {
        String newComments = tableMeta.getComments();
        if (newComments != null) {
            comments = newComments;
        }

        for (TableColumnMeta colMeta : tableMeta.getColumns()) {
            TableColumn col = getColumn(colMeta.getName());
            if (col == null) {
                col = addColumn(colMeta);
            }

            // update the column with the changes
            col.update(colMeta);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns <code>true</code> if this table has no relationships
     *
     * @param withImpliedRelationships boolean
     * @return boolean
     */
    public boolean isOrphan(boolean withImpliedRelationships) {
        if (withImpliedRelationships)
            return getMaxParents() == 0 && getMaxChildren() == 0;

        for (TableColumn column : columns.values()) {
            for (TableColumn parentColumn : column.getParents()) {
                if (!column.getParentConstraint(parentColumn).isImplied())
                    return false;
            }
            for (TableColumn childColumn : column.getChildren()) {
                if (!column.getChildConstraint(childColumn).isImplied())
                    return false;
            }
        }
        return true;
    }

    /**
     * Compare this table to another table.
     * Results are based on 1: identity, 2: table name, 3: schema name<p/>
     * <p>
     * This implementation was put in place to deal with analyzing multiple
     * schemas that contain identically named tables.
     *
     * @see {@link Comparable#compareTo(Object)}
     */
    public int compareTo(Table other) {
        if (other == this)  // fast way out
            return 0;

        return getFullName().compareToIgnoreCase(other.getFullName());
    }

    /**
     * Implementation of {@link Comparator} that sorts {@link TableColumn}s
     * by {@link TableColumn#getId() ID} (ignored if <code>null</code>)
     * followed by {@link TableColumn#getName() Name}.
     */
    public static class ByColumnIdComparator implements Comparator<TableColumn> {
        public int compare(TableColumn column1, TableColumn column2) {
            Object id1 = column1.getId();
            Object id2 = column2.getId();

            if (id1 == null || id2 == null)
                return column1.getName().compareToIgnoreCase(column2.getName());
            if (id1 instanceof Number && id2 instanceof Number)
                return ((Number) id1).intValue() - ((Number) id2).intValue();
            return id1.toString().compareToIgnoreCase(id2.toString());
        }
    }
}