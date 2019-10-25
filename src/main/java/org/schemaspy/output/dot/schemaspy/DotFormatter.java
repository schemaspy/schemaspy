/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
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
package org.schemaspy.output.dot.schemaspy;

import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.view.WriteStats;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 */
public class DotFormatter {

    private final DotConfig dotConfig;
    private final DotFormat dotFormat;

    private final DotSummaryFormatter dotSummaryFormatter;
    private final DotTableFormatter dotTableFormatter;
    private final DotOrphanFormatter dotOrphanFormatter;

    /**
     * Singleton - prevent creation
     */
    public DotFormatter(DotConfig dotConfig) {
        this.dotConfig = dotConfig;
        this.dotFormat = new DotFormat(dotConfig);
        this.dotSummaryFormatter = new DotSummaryFormatter(dotFormat, dotConfig);
        this.dotTableFormatter = new DotTableFormatter(dotFormat, dotConfig);
        this.dotOrphanFormatter = new DotOrphanFormatter(dotFormat, dotConfig);
    }

    public void writeSummaryRealRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        dotSummaryFormatter.writeSummaryRealRelationships(db, tables, compact, showColumns, stats, dot);
    }

    public boolean writeSummaryAllRelationships(Database db, Collection<Table> tables, boolean compact, boolean showColumns, WriteStats stats, PrintWriter dot) {
        return dotSummaryFormatter.writeSummaryAllRelationships(db, tables, compact, showColumns, stats, dot);
    }

    public Set<ForeignKeyConstraint> writeTableRealRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        return dotTableFormatter.writeTableRealRelationships(table, twoDegreesOfSeparation, stats, dot);
    }

    public void writeTableAllRelationships(Table table, boolean twoDegreesOfSeparation, WriteStats stats, PrintWriter dot) {
        dotTableFormatter.writeTableAllRelationships(table, twoDegreesOfSeparation, stats, dot);
    }

    public void writeOrphan(Table table, PrintWriter dot) {
        dotOrphanFormatter.writeOrphan(table, dot);
    }
}
