/*
 * Copyright (C) 2004-2011 John Currier
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017, 2018 Nils Petzaell
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

import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sorts {@link Table}s by their referential integrity constraints.
 * The intent is to have a list of tables in an order that can be used
 * to insert or delete them from a database.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class InsertionOrdered {

    private final Collection<Table> tables;

    public InsertionOrdered(Database db) {
        this(db.getTables());
    }

    /**
     * @param tables Tables to order
     */
    public InsertionOrdered(final Collection<Table> tables) {
        this.tables = tables;
    }

    /**
     * Returns a list of <code>Table</code>s ordered such that parents are listed first
     * and child tables are listed last.
     *
     * @return Returns a list of <code>Table</code>s ordered such that parents are listed first and child tables are listed last.
     */
    public List<Table> getTablesOrderedByRI() {
        List<Table> heads = new ArrayList<>();
        List<Table> tails = new ArrayList<>();
        List<Table> remainingTables = new ArrayList<>(tables);

        // first pass to gather the 'low hanging fruit'
        removeRemotes(remainingTables);
        List<Table> floaters = floaters(remainingTables);
        remainingTables.removeAll(floaters);

        List<Table> unattached = sortTrimmedLevel(floaters);

        while (!remainingTables.isEmpty() && !hasRecursion(remainingTables)) {
            tails.addAll(0, trimLeaves(remainingTables));
            heads.addAll(trimRoots(remainingTables));
        }

        if (hasRecursion(remainingTables)) {
            // get ride of everything that isn't explicitly specified by the database
            for (Table table : remainingTables) {
                table.removeNonRealForeignKeys();
            }
        }

        while (!remainingTables.isEmpty()) {
            boolean hasRecursion = hasRecursion(remainingTables);
            tails.addAll(0, trimLeaves(remainingTables));
            heads.addAll(trimRoots(remainingTables));

            // resolve the recursion by removing a constraint, one by one, 'till the tables are all trimmed
            if (hasRecursion) {
                boolean foundSimpleRecursion = removeSelfReferencingConstraints(remainingTables);
                if (!foundSimpleRecursion) {
                    removeAForeignKeyConstraint(remainingTables);
                }
            }
        }

        // we've gathered all the heads and tails, so combine them here moving 'unattached' tables to the end
        List<Table> ordered = new ArrayList<>(heads.size() + tails.size());

        ordered.addAll(heads);
        ordered.addAll(tails);
        ordered.addAll(unattached);

        return ordered;
    }

    private boolean hasRecursion(List<Table> remainingTables) {
        final List<Table> copy = new LinkedList<>(remainingTables);
        copy.removeIf(Table::isLeaf);
        copy.removeIf(Table::isRoot);
        return copy.size() == remainingTables.size();
    }

    private void removeRemotes(List<Table> remainingTables) {
        remainingTables.stream()
            .filter(Table::isRemote)
            .forEach(table -> {
                // ignore remote tables since there's no way to deal with them
                table.unlinkParents();
                table.unlinkChildren();
            });
        remainingTables.removeIf(Table::isRemote);
    }

    private List<Table> floaters(List<Table> remainingTables) {
        return remainingTables.stream()
                .filter(Table::isFloater)
                .collect(Collectors.toList());
    }

    /**
     * Remove the leaf nodes (tables w/o children)
     *
     * @param tables tables to remove leafs from
     * @return tables removed
     */
    private List<Table> trimLeaves(List<Table> tables) {
        List<Table> leaves = new ArrayList<>();

        for (Table leaf : tables) {
            if (leaf.isLeaf()) {
                leaves.add(leaf);
            }
        }

        tables.removeAll(leaves);

        // now sort them so the ones with large numbers of children show up first (not required, but cool)
        List<Table> trimmedLeaves = sortTrimmedLevel(leaves);

        for (Table trimmedLeaf : trimmedLeaves) {
            // do this after the previous loop to prevent getting leaves before they're ready
            // and so we can sort them correctly
            trimmedLeaf.unlinkParents();
        }

        return trimmedLeaves;
    }

    /**
     * Remove the root nodes (tables w/o parents)
     *
     * @param tables to trim roots from
     * @return tables removed
     */
    private List<Table> trimRoots(List<Table> tables) {
        List<Table> roots = new ArrayList<>();

        for (Table root : tables) {
            if (root.isRoot()) {
                roots.add(root);
            }
        }

        tables.removeAll(roots);

        // now sort them so the ones with large numbers of children show up first (not required, but cool)
        List<Table> trimmedRoots = sortTrimmedLevel(roots);
        for (Table trimmedRoot : trimmedRoots) {
            // do this after the previous loop to prevent getting roots before they're ready
            // and so we can sort them correctly
            trimmedRoot.unlinkChildren();
        }

        return trimmedRoots;
    }

    /**
     * this doesn't change the logical output of the program because all of these (leaves or roots) are at the same logical level
     */
    private List<Table> sortTrimmedLevel(List<Table> tables) {
        /*
          order by
          <ul>
           <li>number of kids (descending)
           <li>number of parents (ascending)
           <li>alpha name (ascending)
          </ul>
         */
        final class TrimComparator implements Comparator<Table> {
            public int compare(Table table1, Table table2) {
                // have to keep track of and use the 'max' versions because
                // by the time we get here we'll (probably?) have no parents or children
                int rc = table2.getMaxChildren() - table1.getMaxChildren();
                if (rc == 0)
                    rc = table1.getMaxParents() - table2.getMaxParents();
                if (rc == 0)
                    rc = table1.compareTo(table2);
                return rc;
            }
        }

        Set<Table> sorter = new TreeSet<>(new TrimComparator());
        sorter.addAll(tables);
        return new ArrayList<>(sorter);
    }

    private boolean removeSelfReferencingConstraints(List<Table> remainingTables) {
        for (Table potentialRecursiveTable : remainingTables) {
            ForeignKeyConstraint recursiveConstraint = potentialRecursiveTable.removeSelfReferencingConstraint();
            if (recursiveConstraint != null) {
                return true;
            }
        }
        return false;
    }

    private void removeAForeignKeyConstraint(List<Table> remainingTables) {
        Table recursiveTable = remainingTables.stream()
            .min((t1, t2) -> {
                // sort on the delta between number of parents and kids so we can
                // target the tables with the biggest delta and therefore the most impact
                // on reducing the smaller of the two
                int rc = Math.abs(t2.getNumChildren() - t2.getNumParents()) - Math.abs(t1.getNumChildren() - t1.getNumParents());
                if (rc == 0)
                    rc = t1.compareTo(t2);
                return rc;
            }).get(); // this one has the largest delta
        recursiveTable.removeAForeignKeyConstraint();
    }
}