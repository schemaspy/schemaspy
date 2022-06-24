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

import org.schemaspy.Config;

import java.util.Collection;

/**
 * Implementation of {@link ProgressListener} that sends its output to the console.
 *
 * @author John Currier
 * @author Thomas Traude
 */
public class Tracked implements ProgressListener {

    private long startedAt;
    private long startedGatheringAt;
    private long startedConnectingAt;
    private long startedGraphingSummariesAt;
    private long startedGraphingDetailsAt;
    private long finishedAt;

    public Tracked() {
        startedAt = System.currentTimeMillis();
    }

    @Override
    public void startedGatheringDetails() {
        startedGatheringAt = System.currentTimeMillis();
    }

    @Override
    public void gatheringDetailsProgressed(Table table) { }

    @Override
    public long startedConnectingTables() {
        startedConnectingAt = System.currentTimeMillis();
        return startedConnectingAt - startedGatheringAt;
    }

    @Override
    public void connectingTablesProgressed(Table table) { }

    @Override
    public long startedGraphingSummaries() {
        startedGraphingSummariesAt = System.currentTimeMillis();
        return startedGraphingSummariesAt - startedConnectingAt;
    }

    @Override
    public void graphingSummaryProgressed() { }

    @Override
    public long startedGraphingDetails() {
        startedGraphingDetailsAt = System.currentTimeMillis();
        return startedGraphingDetailsAt - startedGraphingSummariesAt;
    }

    @Override
    public void graphingDetailsProgressed(Table table) { }

    @Override
    public long finishedGatheringDetails() {
        finishedAt = System.currentTimeMillis();
        return finishedAt - startedGraphingDetailsAt;
    }

    @Override
    public long finished(Collection<Table> tables, Config config) {
        finishedAt = System.currentTimeMillis();
        return finishedAt - startedAt;
    }
}
