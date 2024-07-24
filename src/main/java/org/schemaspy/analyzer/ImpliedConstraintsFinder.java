/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
 * Copyright (C) 2017 Mårten Bohlin
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
package org.schemaspy.analyzer;

import org.schemaspy.model.DatabaseObject;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ImpliedConstraintsFinder {

    public List<ImpliedForeignKeyConstraint> find(Collection<Table> tables) {
        List<TableColumn> columnsWithoutParents =
            tables
                .stream()
                .map(Table::getColumns)
                .flatMap(Collection::stream)
                .filter(this::noParent)
                .sorted(byTable)
                .collect(Collectors.toList());

        Map<DatabaseObject, Table> keyedTablesByPrimary = primaryKeys(tables);

        List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList<>();

        for (TableColumn childColumn : columnsWithoutParents) {
            DatabaseObject columnWithoutParent = new DatabaseObject(childColumn);

            // search for Parent(PK) table
            Table primaryTable = findPrimaryTable(columnWithoutParent, keyedTablesByPrimary);

            if (primaryTable != null && primaryTable != childColumn.getTable()) {
                // can't match up multiples...yet...==> so checks only first  PK column.
                TableColumn parentColumn = primaryTable.getPrimaryColumns().get(0);
                // make sure the potential child->parent relationships isn't already a
                // parent->child relationship
                if (parentColumn.getParentConstraint(childColumn) == null) {
                    // ok, we've found a potential relationship with a column matches a primary
                    // key column in another table and isn't already related to that column
                    impliedConstraints.add(new ImpliedForeignKeyConstraint(parentColumn, childColumn));
                }
            }
        }
        return impliedConstraints;
    }

    private boolean noParent(TableColumn column) {
        //TODO fixed column name "LanguageId" should be moved to schemaspy properties
        return !column.isForeignKey()
               && !column.isPrimary()
               && column.allowsImpliedParents()
               && !"LanguageId".equals(column.getName());
    }

    private Comparator<TableColumn> byTable = (column1, column2) -> {
        int rc = column1.getTable().compareTo(column2.getTable());
        if (rc == 0) {
            rc = column1.getName().compareToIgnoreCase(column2.getName());
        }
        return rc;
    };

    private Map<DatabaseObject, Table> primaryKeys(Collection<Table> tables) {
        Map<DatabaseObject, Table> keyedTablesByPrimary = new TreeMap<>();

        for (Table table : tables) {
            List<TableColumn> tablePrimaries = table.getPrimaryColumns();
            if (tablePrimaries.size() == 1 || tablePrimaries.stream().anyMatch(t -> "LanguageId".equals(t.getName()))) { // can't match up multiples...yet...
                TableColumn tableColumn = tablePrimaries.get(0);
                DatabaseObject primary = new DatabaseObject(tableColumn);
                if (tableColumn.allowsImpliedChildren()) {
                    // new primary key name/type
                    keyedTablesByPrimary.put(primary, table);
                }
            }
        }
        return keyedTablesByPrimary;
    }

    private Table findPrimaryTable(DatabaseObject columnWithoutParent, Map<DatabaseObject, Table> keyedTablesByPrimary) {
        Table primaryTable = null;
        for (Map.Entry<DatabaseObject, Table> entry : keyedTablesByPrimary.entrySet()) {
            DatabaseObject key = entry.getKey();
            if (
                nameMatches(columnWithoutParent.getName(), key.getName(), entry.getValue().getName())
                && typeMatches(columnWithoutParent, key)
            ) {
                // if child column refrencing multiple PK(Parent) tables then don't create implied relationship and exit the loop.
                // one column can reference only one parent table.!
                if (Objects.nonNull(primaryTable)) {
                    return null;
                }
                primaryTable = entry.getValue();
            }
        }
        return primaryTable;
    }

    private boolean nameMatches(String columnWithoutParent, String primaryKey, String primaryKeyTable) {
        return columnWithoutParent.compareToIgnoreCase(primaryKey) == 0
               || columnWithoutParent.matches("(?i).*_" + Pattern.quote(primaryKey))
               || columnWithoutParent.matches("(?i)" + Pattern.quote(primaryKeyTable) + ".*" + Pattern.quote(primaryKey));
    }

    private boolean typeMatches(DatabaseObject orphan, DatabaseObject primaryKey) {
        return ((orphan.getType() != null && primaryKey.getType() != null
                 && orphan.getType().compareTo(primaryKey.getType()) == 0)
                || orphan.getTypeName()
                         .compareToIgnoreCase(primaryKey.getTypeName()) == 0) && orphan.getLength() - primaryKey.getLength() == 0;
    }
}
