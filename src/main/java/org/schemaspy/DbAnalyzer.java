/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 Mårten Bohlin
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
package org.schemaspy;

import org.schemaspy.model.*;
import org.schemaspy.util.Inflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Mårten Bohlin
 */
public class DbAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
    public static List<ImpliedForeignKeyConstraint> getImpliedConstraints(Collection<Table> tables) {
        List<TableColumn> columnsWithoutParents = new ArrayList<TableColumn>();
        Map<DatabaseObject, Table> keyedTablesByPrimary = new TreeMap<DatabaseObject, Table>();
        
        // gather all the primary key columns and columns without parents
        for (Table table : tables) {
            List<TableColumn> tablePrimaries = table.getPrimaryColumns();
            if (tablePrimaries.size() == 1 || tablePrimaries.stream().anyMatch(t -> t.getName().equals("LanguageId"))) { // can't match up multiples...yet...
            	TableColumn tableColumn = tablePrimaries.get(0);
                DatabaseObject primary = new DatabaseObject(tableColumn);
                if (tableColumn.allowsImpliedChildren()) {
                    // new primary key name/type 
                    keyedTablesByPrimary.put(primary, table);
                }
            }

            //TODO fixed column name "LanguageId" should be moved to schemaspy properties
            for (TableColumn column : table.getColumns()) {
                if (!column.isForeignKey() && !column.isPrimary() && column.allowsImpliedParents() && !column.getName().equals("LanguageId"))
                    columnsWithoutParents.add(column);
            }
        }

        sortColumnsByTable(columnsWithoutParents);
        List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList<ImpliedForeignKeyConstraint>();
        
        for (TableColumn childColumn : columnsWithoutParents) {
            DatabaseObject columnWithoutParent = new DatabaseObject(childColumn);
            
            // search for Parent(PK) table
        	Table primaryTable = null;
        	Integer numPrimaryTableFound=0;
			for (Map.Entry<DatabaseObject, Table> entry : keyedTablesByPrimary.entrySet()) {
				DatabaseObject key = entry.getKey();
				if (columnWithoutParent.getName().compareToIgnoreCase(key.getName()) == 0
						// if adress_id=adress_id OR shipping_adress_id like &description%_adress_id
						|| columnWithoutParent.getName().matches("(?i).*_" + Pattern.quote(key.getName()))
						// if order.adressid=>adress.id. find FKs that made from %parentTablename%PKcol%
						// if order.adress_id=>adress.id. find FKs that made from %tablename%_%PKcol%
						|| columnWithoutParent.getName().matches("(?i)" + Pattern.quote(entry.getValue().getName()) + ".*" + Pattern.quote(key.getName()))
						) {
					// check f columnTypes Or ColumnTypeNames are same.
					if ((columnWithoutParent.getType() != null && key.getType() != null
							&& columnWithoutParent.getType().compareTo(key.getType()) == 0)
							|| columnWithoutParent.getTypeName().compareToIgnoreCase(key.getTypeName()) == 0) {
						// check if column lengths are same.
						if (columnWithoutParent.getLength() - key.getLength() == 0) {
							// found parent table.
							primaryTable = entry.getValue();
							numPrimaryTableFound++;
							// if child column refrencing multiple PK(Parent) tables then dont create implied relationship and exit the loop.
							// one column can reference only one parent table.! 
							if (numPrimaryTableFound>1) {
								primaryTable=null;
								break;
							}
						}
					}
				}
			}
            
            if (primaryTable != null && primaryTable != childColumn.getTable()) {
                //Optional<DatabaseObject> databaseObject = primaryColumns.stream().filter(d-> d.getName().equals(primaryTable.getName())).findFirst();
                //if (databaseObject.isPresent()) {
                //    TableColumn parentColumn = primaryTable.getColumn(databaseObject.get().getOrginalName());
            	
            	// // can't match up multiples...yet...==> so checks only first  PK column.
            	TableColumn parentColumn = primaryTable.getPrimaryColumns().get(0);
                // make sure the potential child->parent relationships isn't already a
                // parent->child relationship
                if (parentColumn.getParentConstraint(childColumn) == null) {
                    // ok, we've found a potential relationship with a column matches a primary
                    // key column in another table and isn't already related to that column
                    impliedConstraints.add(new ImpliedForeignKeyConstraint(parentColumn, childColumn));
                }
                //}
            }
        }
        

        return impliedConstraints;
    }

    /**
     * Ruby on Rails-based databases typically have no real referential integrity
     * constraints.  Instead they have a somewhat unusual way of associating
     * columns to primary keys.<p>
     *
     * Basically all tables have a primary key named <code>ID</code>.
     * All tables are named plural names.
     * The columns that logically reference that <code>ID</code> are the singular
     * form of the table name suffixed with <code>_ID</code>.<p>
     *
     * A side-effect of calling this method is that the returned collection of
     * constraints will be "tied into" the associated tables.
     *
     * @param tables
     * @return List of {@link RailsForeignKeyConstraint}s
     */
    public static List<RailsForeignKeyConstraint> getRailsConstraints(Map<String, Table> tables) {
        List<RailsForeignKeyConstraint> railsConstraints = new ArrayList<RailsForeignKeyConstraint>(tables.size());

        // iterate thru each column in each table looking for columns that
        // match Rails naming conventions
        for (Table table : tables.values()) {
            for (TableColumn column : table.getColumns()) {
                String columnName = column.getName().toLowerCase();
                if (!column.isForeignKey() && column.allowsImpliedParents() && columnName.endsWith("_id")) {
                    String singular = columnName.substring(0, columnName.length() - 3);
                    String primaryTableName = Inflection.pluralize(singular);
                    Table primaryTable = tables.get(primaryTableName);
                    if (primaryTable != null) {
                        TableColumn primaryColumn = primaryTable.getColumn("ID");
                        if (primaryColumn != null) {
                            railsConstraints.add(new RailsForeignKeyConstraint(primaryColumn, column));
                        }
                    }
                }
            }
        }

        return railsConstraints;
    }

    /**
     * Returns a <code>List</code> of all of the <code>ForeignKeyConstraint</code>s
     * used by the specified tables.
     *
     * @param tables Collection
     * @return List
     */
    public static List<ForeignKeyConstraint> getForeignKeyConstraints(Collection<Table> tables) {
        List<ForeignKeyConstraint> constraints = new ArrayList<ForeignKeyConstraint>();

        for (Table table : tables) {
            constraints.addAll(table.getForeignKeys());
        }

        return constraints;
    }

    public static List<Table> getOrphans(Collection<Table> tables) {
        List<Table> orphans = new ArrayList<Table>();

        for (Table table : tables) {
            if (table.isOrphan(false) && !table.isView()) {
                orphans.add(table);
            }
        }

        return sortTablesByName(orphans);
    }

    /**
     * Return a list of <code>Table</code>s that have neither an index nor a primary key.
     */
    public static List<Table> getTablesWithoutIndexes(Collection<Table> tables) {
        List<Table> withoutIndexes = new ArrayList<Table>();

        for (Table table : tables) {
            if (table.getIndexes().isEmpty() && !table.isView() && !table.isLogical())
                withoutIndexes.add(table);
        }

        return sortTablesByName(withoutIndexes);
    }

    public static List<Table> getTablesWithIncrementingColumnNames(Collection<Table> tables) {
        List<Table> denormalizedTables = new ArrayList<Table>();

        for (Table table : tables) {
            Map<String, Long> columnPrefixes = new HashMap<String, Long>();

            for (TableColumn column : table.getColumns()) {
                // search for columns that start with the same prefix
                // and end in an incrementing number

                String columnName = column.getName();
                String numbers = null;
                for (int i = columnName.length() - 1; i > 0; --i) {
                    if (Character.isDigit(columnName.charAt(i))) {
                        numbers = String.valueOf(columnName.charAt(i)) + (numbers == null ? "" : numbers);
                    } else {
                        break;
                    }
                }

                // attempt to detect where they had an existing column
                // and added a "column2" type of column (we'll call this one "1")
                if (numbers == null) {
                    numbers = "1";
                    columnName = columnName + numbers;
                }

                // see if we've already found a column with the same prefix
                // that had a numeric suffix +/- 1.
                String prefix = columnName.substring(0, columnName.length() - numbers.length());
                long numeric = Long.parseLong(numbers);
                Long existing = columnPrefixes.get(prefix);
                if (existing != null && Math.abs(existing - numeric) == 1) {
                    // found one so add it to our list and stop evaluating this table
                    denormalizedTables.add(table);
                    break;
                }
                columnPrefixes.put(prefix, numeric);
            }
        }

        return sortTablesByName(denormalizedTables);
    }

    public static List<Table> getTablesWithOneColumn(Collection<Table> tables) {
        List<Table> singleColumnTables = new ArrayList<Table>();

        for (Table table : tables) {
            if (table.getColumns().size() == 1)
                singleColumnTables.add(table);
        }

        return sortTablesByName(singleColumnTables);
    }

    public static List<Table> sortTablesByName(List<Table> tables) {
        tables.sort(Table::compareTo);

        return tables;
    }

    public static List<TableColumn> sortColumnsByTable(List<TableColumn> columns) {
        columns.sort((column1, column2) -> {
            int rc = column1.getTable().compareTo(column2.getTable());
            if (rc == 0)
                rc = column1.getName().compareToIgnoreCase(column2.getName());
            return rc;
        });

        return columns;
    }

    /**
     * Returns a list of columns that have the word "NULL" or "null" as their default value
     * instead of the likely candidate value null.
     *
     * @param tables Collection
     * @return List
     */
    public static List<TableColumn> getDefaultNullStringColumns(Collection<Table> tables) {
        List<TableColumn> defaultNullStringColumns = new ArrayList<TableColumn>();

        for (Table table : tables) {
            for (TableColumn column : table.getColumns()) {
                Object defaultValue = column.getDefaultValue();
                if (defaultValue != null && defaultValue instanceof String) {
                    String defaultString = defaultValue.toString();
                    if (defaultString.trim().equalsIgnoreCase("'null'")) {
                        defaultNullStringColumns.add(column);
                    }
                }
            }
        }

        return sortColumnsByTable(defaultNullStringColumns);
    }

    /**
     * getSchemas - returns a List of catalog names (Strings)
     *
     * @param meta DatabaseMetaData
     */
    public static List<String> getCatalogs(DatabaseMetaData meta) throws SQLException {
        List<String> catalogs = new ArrayList<String>();

        ResultSet rs = meta.getCatalogs();
        while (rs.next()) {
            catalogs.add(rs.getString("TABLE_CAT"));
        }
        rs.close();

        return catalogs;
    }

    /**
     * getSchemas - returns a List of schema names (Strings)
     *
     * @param meta DatabaseMetaData
     */
    public static List<String> getSchemas(DatabaseMetaData meta) throws SQLException {
        List<String> schemas = new ArrayList<String>();

        ResultSet rs = meta.getSchemas();
        while (rs.next()) {
            schemas.add(rs.getString("TABLE_SCHEM"));
        }
        rs.close();

        return schemas;
    }

    /**
     * getSchemas - returns a List of schema names (Strings) that contain tables
     *
     * @param meta DatabaseMetaData
     */
    public static List<String> getPopulatedSchemas(DatabaseMetaData meta) throws SQLException {
        return getPopulatedSchemas(meta, ".*", false);
    }

    /**
     * getSchemas - returns a List of schema names (Strings) that contain tables and
     * match the <code>schemaSpec</code> regular expression
     *
     * @param meta DatabaseMetaData
     */
    public static List<String> getPopulatedSchemas(DatabaseMetaData meta, String schemaSpec, boolean isCatalog) throws SQLException {
        Set<String> schemas = new TreeSet<String>(); // alpha sorted
        Pattern schemaRegex = Pattern.compile(schemaSpec);

        for (String schema : (isCatalog ? getCatalogs(meta) : getSchemas(meta))) {
            if (schemaRegex.matcher(schema).matches()) {
                ResultSet rs = null;
                try {
                    rs = meta.getTables(null, schema, "%", null);
                    if (rs.next()) {
                        LOGGER.debug("Including schema {}: matches + \"{}\" and contains tables", schema, schemaRegex);
                        schemas.add(schema);
                    } else {
                        LOGGER.debug("Excluding schema {}: matches \"{}\" but contains no tables", schema, schemaRegex);
                    }
                } catch (SQLException ignore) {
                } finally {
                    if (rs != null)
                        rs.close();
                }
            } else {
                LOGGER.debug("Excluding schema {}: doesn't match \"{}" + '"', schema, schemaRegex);
            }
        }

        return new ArrayList<String>(schemas);
    }

    /**
     * For debugging/analyzing result sets
     * @param rs ResultSet
     * @throws SQLException
     */
    public static void dumpResultSetRow(ResultSet rs, String description) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int numColumns = meta.getColumnCount();
        System.out.println(numColumns + " columns of " + description + ":");
        for (int i = 1; i <= numColumns; ++i) {
            System.out.print(meta.getColumnLabel(i));
            System.out.print(": ");
            System.out.print(String.valueOf(rs.getString(i)));
            System.out.print("\t");
        }
        System.out.println();
    }
}
