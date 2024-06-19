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

import java.util.stream.StreamSupport;
import org.schemaspy.model.*;
import org.schemaspy.util.Filtered;
import org.schemaspy.util.Inflection;
import org.schemaspy.util.WhenFalse;
import org.schemaspy.util.WhenIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
 * @author Nils Petzaell
 */
public class DbAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        List<RailsForeignKeyConstraint> railsConstraints = new ArrayList<>(tables.size());

        // iterate thru each column in each table looking for columns that
        // match Rails naming conventions
        for (Table table : tables.values()) {
            for (TableColumn column : table.getColumns()) {
                if (!column.isForeignKey() && column.allowsImpliedParents()) {
                    Table primaryTable = railsPrimaryTable(column, tables);
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

    private static Table railsPrimaryTable(final TableColumn column, final Map<String, Table> tables) {
        Table primaryTable = null;
        String columnName = column.getName().toLowerCase();
        if (columnName.endsWith("_id")) {
            String singular = columnName.substring(0, columnName.length() - "_id".length());
            String primaryTableName = Inflection.pluralize(singular);
            primaryTable = tables.get(primaryTableName);
        }
        return primaryTable;
    }

    /**
     * Returns a <code>List</code> of all of the <code>ForeignKeyConstraint</code>s
     * used by the specified tables.
     *
     * @param tables Collection
     * @return List
     */
    public static List<ForeignKeyConstraint> getForeignKeyConstraints(Collection<Table> tables) {
        List<ForeignKeyConstraint> constraints = new ArrayList<>();

        for (Table table : tables) {
            constraints.addAll(table.getForeignKeys());
        }

        return constraints;
    }

    /**
     * Return a list of <code>Table</code>s that have neither an index nor a primary key.
     */
    public static List<Table> getTablesWithoutIndexes(Collection<Table> tables) {
        List<Table> withoutIndexes = new ArrayList<>();

        for (Table table : tables) {
            if (table.getIndexes().isEmpty() && !table.isView() && !table.isLogical())
                withoutIndexes.add(table);
        }

        return sortTablesByName(withoutIndexes);
    }

    public static List<Table> getTablesWithIncrementingColumnNames(Collection<Table> tables) {
        List<Table> denormalizedTables = new ArrayList<>();

        for (Table table : tables) {
            Map<String, Long> columnPrefixes = new HashMap<>();

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
        List<Table> singleColumnTables = new ArrayList<>();

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
        List<TableColumn> defaultNullStringColumns = new ArrayList<>();

        for (Table table : tables) {
            for (TableColumn column : table.getColumns()) {
                Object defaultValue = column.getDefaultValue();
                if (defaultValue instanceof String) {
                    String defaultString = defaultValue.toString();
                    if ("'null'".equalsIgnoreCase(defaultString.trim())) {
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
        List<String> catalogs = new ArrayList<>();

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
        List<String> schemas = new ArrayList<>();

        ResultSet rs = meta.getSchemas();
        while (rs.next()) {
            schemas.add(rs.getString("TABLE_SCHEM"));
        }
        rs.close();

        return schemas;
    }

    /**
     * getSchemas - returns a List of schema names (Strings) that contain tables, fallback to catalogs
     *
     * @param meta DatabaseMetaData
     */
    public static List<String> getPopulatedSchemas(DatabaseMetaData meta) throws SQLException {
        return getPopulatedSchemas(meta, ".*");
    }

    /**
     * getSchemas - returns a List of schema names (Strings) that contain tables, fallback to catalogs
     *
     * @param meta DatabaseMetaData
     * @param schemaSpec filter for schema/catalog
     */
    public static List<String> getPopulatedSchemas(DatabaseMetaData meta, String schemaSpec) throws SQLException {
        List<String> populatedSchemas = getPopulatedSchemas(meta, schemaSpec, getSchemas(meta));
        if (populatedSchemas.isEmpty()) {
            return getPopulatedSchemas(meta, schemaSpec, getCatalogs(meta));
        }
        return populatedSchemas;
    }

    /**
     * getSchemas - returns a List of schema names (Strings) that contain tables and
     * match the <code>schemaSpec</code> regular expression, can look in catalogs
     *
     * @param meta DatabaseMetaData
     * @param schemaSpec filter for catalog or schema
     * @param candidates schemas to consider
     */
    public static List<String> getPopulatedSchemas(
        DatabaseMetaData meta,
        String schemaSpec,
        final List<String> candidates
    ) {
        Pattern schemaRegex = Pattern.compile(schemaSpec);

        final Iterable<String> matched = new Filtered<>(
            candidates,
            new WhenFalse<>(
                schema -> schemaRegex.matcher(schema).matches(),
                schema -> LOGGER.debug("Excluding schema {}: doesn't match '{}'", schema, schemaRegex)
            )
        );

        final Iterable<String> populated = new Filtered<>(
            matched,
            new WhenIf<>(
                schema -> hasTables(meta, schema),
                schema -> LOGGER.debug("Including schema {}: matches + \"{}\" and contains tables", schema, schemaRegex),
                schema -> LOGGER.debug("Excluding schema {}: matches \"{}\" but contains no tables", schema, schemaRegex)
            )
        );

        return StreamSupport.stream(populated.spliterator(), false)
            .distinct()
            .sorted()
            .toList();
    }

    public static boolean hasTables(
        final DatabaseMetaData meta,
        final String schema
    ) {
        try(final ResultSet rs = meta.getTables(null, schema, "%", null)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
