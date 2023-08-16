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
    private long finishedAt;

    public Tracked() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    public void startCollectingTablesViews() {
        startedCollectingTablesViewsAt = System.currentTimeMillis();
    }

    @Override
    public void tableViewCollected(Table table) { }

    /**
     * Assumes <code>startedGatheringDetails</code> has already been called.
     */
    @Override
    public long startConnectingTablesViews() {
        startedConnectingTablesViewsAt = System.currentTimeMillis();
        return startedConnectingTablesViewsAt - startedCollectingTablesViewsAt;
    }

    @Override
    public void connectedTableView(Table table) { }

    /**
     * Assumes <code>startedConnectingTables</code> has already been called.
     */
    @Override
    public long startCreatingSummaries() {
        startedCreatingSummariesAt = System.currentTimeMillis();
        return startedCreatingSummariesAt - startedConnectingTablesViewsAt;
    }

    @Override
    public void createdSummary() { }

    /**
     * Assumes <code>startedGraphingSummaries</code> has already been called.
     */
    @Override
    public long startCreatingTablePages() {
        startedCreatingTablePagesAt = System.currentTimeMillis();
        return startedCreatingTablePagesAt - startedCreatingSummariesAt;
    }

    @Override
    public void createdTablePage(Table table) { }

    /**
     * Assumes <code>startedGraphingDetails</code> has already been called.
     */
    @Override
    public long finishedCreatingTablePages() {
        finishedAt = System.currentTimeMillis();
        return finishedAt - startedCreatingTablePagesAt;
    }

    @Override
    public long finished(Collection<Table> tablesg) {
        finishedAt = System.currentTimeMillis();
        return finishedAt - startedAt;
    }
}
