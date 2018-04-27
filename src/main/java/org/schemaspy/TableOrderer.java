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

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;

import java.util.*;

/**
 * Sorts {@link Table}s by their referential integrity constraints.
 * The intent is to have a list of tables in an order that can be used
 * to insert or delete them from a database.
 *
 * @author John Currier
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class TableOrderer {
    /**
     * Returns a list of <code>Table</code>s ordered such that parents are listed first
     * and child tables are listed last.
     *
     * <code>recursiveConstraints</code> gets populated with <code>TableConstraint</code>s
     * that had to be removed to resolve the returned list.
     * @param tables Tables to order
     * @param recursiveConstraints gets populated with TableConstraints that had to be removed to resolve the returned list
     * @return Returns a list of <code>Table</code>s ordered such that parents are listed first and child tables are listed last.
     */
    public List<Table> getTablesOrderedByRI(Collection<Table> tables, Collection<ForeignKeyConstraint> recursiveConstraints) {
        List<Table> heads = new ArrayList<>();
        List<Table> tails = new ArrayList<>();
        List<Table> remainingTables = new ArrayList<>(tables);
        List<Table> unattached = new ArrayList<>();

        // first pass to gather the 'low hanging fruit'
        removeRemotesAndUnattached(remainingTables, unattached);

        unattached = sortTrimmedLevel(unattached);
        boolean prunedNonReals = false;

        while (!remainingTables.isEmpty()) {
            int tablesLeft = remainingTables.size();
            tails.addAll(0, trimLeaves(remainingTables));
            heads.addAll(trimRoots(remainingTables));

            // if we could't trim anything then there's recursion....
            // resolve it by removing a constraint, one by one, 'till the tables are all trimmed
            if (tablesLeft == remainingTables.size()) {
                if (!prunedNonReals) {
                    // get ride of everything that isn't explicitly specified by the database
                    for (Table table : remainingTables) {
                        table.removeNonRealForeignKeys();
                    }

                    prunedNonReals = true;
                    continue;
                }

                boolean foundSimpleRecursion = removeSelfReferencingConstraints(remainingTables, recursiveConstraints);
                removeAForeignKeyConstraint(recursiveConstraints, remainingTables, foundSimpleRecursion);
            }
        }

        // we've gathered all the heads and tails, so combine them here moving 'unattached' tables to the end
        List<Table> ordered = new ArrayList<>(heads.size() + tails.size());

        ordered.addAll(heads);
        ordered.addAll(tails);
        ordered.addAll(unattached);

        return ordered;
    }

    private static void removeRemotesAndUnattached(List<Table> remainingTables, List<Table> unattached) {
        for (Iterator<Table> iter = remainingTables.iterator(); iter.hasNext(); ) {
            Table table = iter.next();
            if (table.isRemote()) {
                // ignore remote tables since there's no way to deal with them
                table.unlinkParents();
                table.unlinkChildren();
                iter.remove();
            } else if (table.isLeaf() && table.isRoot()) {
                // floater, so add it to 'unattached'
                unattached.add(table);
                iter.remove();
            }
        }
    }

    /**
     * Remove the leaf nodes (tables w/o children)
     *
     * @param tables tables to remove leafs from
     * @return tables removed
     */
    private static List<Table> trimLeaves(List<Table> tables) {
        List<Table> leaves = new ArrayList<>();

        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            Table leaf = iter.next();
            if (leaf.isLeaf()) {
                leaves.add(leaf);
                iter.remove();
            }
        }

        // now sort them so the ones with large numbers of children show up first (not required, but cool)
        leaves = sortTrimmedLevel(leaves);
        iter = leaves.iterator();
        while (iter.hasNext()) {
            // do this after the previous loop to prevent getting leaves before they're ready
            // and so we can sort them correctly
            iter.next().unlinkParents();
        }

        return leaves;
    }

    /**
     * Remove the root nodes (tables w/o parents)
     *
     * @param tables to trim roots from
     * @return tables removed
     */
    private static List<Table> trimRoots(List<Table> tables) {
        List<Table> roots = new ArrayList<>();

        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            Table root = iter.next();
            if (root.isRoot()) {
                roots.add(root);
                iter.remove();
            }
        }

        // now sort them so the ones with large numbers of children show up first (not required, but cool)
        roots = sortTrimmedLevel(roots);
        iter = roots.iterator();
        while (iter.hasNext()) {
            // do this after the previous loop to prevent getting roots before they're ready
            // and so we can sort them correctly
            iter.next().unlinkChildren();
        }

        return roots;
    }

    /**
     * this doesn't change the logical output of the program because all of these (leaves or roots) are at the same logical level
     */
    private static List<Table> sortTrimmedLevel(List<Table> tables) {
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

    private static boolean removeSelfReferencingConstraints(List<Table> remainingTables, Collection<ForeignKeyConstraint> recursiveConstraints) {
        boolean foundSimpleRecursion = false;
        for (Table potentialRecursiveTable : remainingTables) {
            ForeignKeyConstraint recursiveConstraint = potentialRecursiveTable.removeSelfReferencingConstraint();
            if (recursiveConstraint != null) {
                recursiveConstraints.add(recursiveConstraint);
                foundSimpleRecursion = true;
            }
        }
        return foundSimpleRecursion;
    }

    private static void removeAForeignKeyConstraint(Collection<ForeignKeyConstraint> recursiveConstraints, List<Table> remainingTables, boolean foundSimpleRecursion) {
        if (!foundSimpleRecursion) {
            // expensive comparison, but we're down to the end of the tables so it shouldn't really matter
            Set<Table> byParentChildDelta = new TreeSet<>((t1, t2) -> {
                // sort on the delta between number of parents and kids so we can
                // target the tables with the biggest delta and therefore the most impact
                // on reducing the smaller of the two
                int rc = Math.abs(t2.getNumChildren() - t2.getNumParents()) - Math.abs(t1.getNumChildren() - t1.getNumParents());
                if (rc == 0)
                    rc = t1.compareTo(t2);
                return rc;
            });
            byParentChildDelta.addAll(remainingTables);
            Table recursiveTable = byParentChildDelta.iterator().next(); // this one has the largest delta
            ForeignKeyConstraint removedConstraint = recursiveTable.removeAForeignKeyConstraint();
            recursiveConstraints.add(removedConstraint);
        }
    }
}