/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2017 Thomas Traude
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

import java.util.Collection;

/**
 * Implementation of {@link ProgressListener}.
 *
 * @author John Currier
 * @author Thomas Traude
 */
public class Tracked implements ProgressListener {

    private final long startedAt;
    private long startedCollectingTablesViewsAt;
    private long startedConnectingTablesViewsAt;
    private long startedCreatingSummariesAt;
    private long startedCreatingTablePagesAt;

    public Tracked() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    public void startCollectingTablesViews() {
        startedCollectingTablesViewsAt = System.currentTimeMillis();
    }

    @Override
    public void tableViewCollected(Table table) {
        // Empty since we don't measure this.
    }

    /**
     * Assumes <code>startCollectingTablesViews</code> has already been called.
     */
    @Override
    public long finishedCollectingTablesViews() {
        return System.currentTimeMillis() - startedCollectingTablesViewsAt;
    }

    @Override
    public void startConnectingTablesViews() {
        startedConnectingTablesViewsAt = System.currentTimeMillis();
    }

    @Override
    public void connectedTableView(Table table) {
        // Empty since we don't measure this.
    }

    /**
     * Assumes <code>startConnectingTablesViews</code> has already been called.
     */
    @Override
    public long finishedConnectingTablesViews() {
        return System.currentTimeMillis() - startedConnectingTablesViewsAt;
    }

    @Override
    public void startCreatingSummaries() {
        startedCreatingSummariesAt = System.currentTimeMillis();
    }

    @Override
    public void createdSummary() {
        // Empty since we don't measure this.
    }

    /**
     * Assumes <code>startCreatingSummaries</code> has already been called.
     */
    @Override
    public long finishedCreatingSummaries() {
        return System.currentTimeMillis() - startedCreatingSummariesAt;
    }

    @Override
    public void startCreatingTablePages() {
        startedCreatingTablePagesAt = System.currentTimeMillis();
    }

    @Override
    public void createdTablePage(Table table) {
        // Empty since we don't measure this.
    }

    /**
     * Assumes <code>startedGraphingDetails</code> has already been called.
     */
    @Override
    public long finishedCreatingTablePages() {
        return System.currentTimeMillis() - startedCreatingTablePagesAt;
    }

    @Override
    public long finished(Collection<Table> tablesg) {
        return System.currentTimeMillis() - startedAt;
    }
}
