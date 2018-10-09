/*
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
package org.schemaspy.output.html.mustache.diagrams;

import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.model.Database;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.util.Writers;
import org.schemaspy.view.DotFormatter;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MustacheSummaryDiagramFactory {

    private final DotFormatter dotProducer;
    private final MustacheDiagramFactory mustacheDiagramFactory;
    private final ImpliedConstraintsFinder impliedConstraintsFinder;
    private final File outputDir;
    private final Path summaryDir;

    public MustacheSummaryDiagramFactory(DotFormatter dotProducer, MustacheDiagramFactory mustacheDiagramFactory, ImpliedConstraintsFinder impliedConstraintsFinder, File outputDir) {
        this.dotProducer = dotProducer;
        this.mustacheDiagramFactory = mustacheDiagramFactory;
        this.impliedConstraintsFinder = impliedConstraintsFinder;
        this.outputDir = outputDir;
        this.summaryDir = outputDir.toPath().resolve("diagrams").resolve("summary");
    }

    public MustacheSummaryDiagramResults generateSummaryDiagrams(
            Database database,
            Collection<Table> tables,
            boolean includeImpliedConstraints,
            boolean showDetailedTables,
            ProgressListener progressListener
    ) throws IOException {
        Files.createDirectories(summaryDir);
        List<MustacheTableDiagram> diagrams = new ArrayList<>();
        // generate the compact form of the relationships .dot file
        String dotBaseFilespec = "relationships";
        WriteStats stats = new WriteStats(tables);
        File realCompactDot = summaryDir.resolve(dotBaseFilespec + ".real.compact.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(realCompactDot)) {
            dotProducer.writeRealRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
        }
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;


        if (hasRealRelationships) {
            MustacheTableDiagram realCompactDiagram = mustacheDiagramFactory.generateSummaryDiagram("Compact", realCompactDot, dotBaseFilespec + ".real.compact");
            realCompactDiagram.setActive(true);
            diagrams.add(realCompactDiagram);
            // real relationships exist so generate the 'big' form of the relationships .dot file
            File realLagerDot = summaryDir.resolve(dotBaseFilespec + ".real.large.dot").toFile();
            try (PrintWriter out = Writers.newPrintWriter(realLagerDot)) {
                dotProducer.writeRealRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
            }
            MustacheTableDiagram realLargeDiagram = mustacheDiagramFactory.generateSummaryDiagram("Large", realLagerDot, dotBaseFilespec + ".real.compact");
            diagrams.add(realLargeDiagram);
        } else {
            Files.deleteIfExists(realCompactDot.toPath());
        }

        // getting implied constraints has a side-effect of associating the parent/child tables, so don't do it
        // here unless they want that behavior
        List<ImpliedForeignKeyConstraint> impliedConstraints = new ArrayList();
        if (includeImpliedConstraints)
            impliedConstraints.addAll(impliedConstraintsFinder.find(tables));

        progressListener.graphingSummaryProgressed();
        if (!impliedConstraints.isEmpty()) {
            File impliedCompactDot = summaryDir.resolve(dotBaseFilespec + ".implied.compact.dot").toFile();
            try (PrintWriter out = Writers.newPrintWriter(impliedCompactDot)) {
                dotProducer.writeAllRelationships(database, tables, true, showDetailedTables, stats, out, outputDir);
            }
            MustacheTableDiagram impliedCompactDiagram = mustacheDiagramFactory.generateSummaryDiagram("Compact Implied", impliedCompactDot, dotBaseFilespec + ".implied.compact");
            impliedCompactDiagram.setIsImplied(true);
            diagrams.add(impliedCompactDiagram);

            File impliedLargeDot = summaryDir.resolve(dotBaseFilespec + ".implied.large.dot").toFile();
            try (PrintWriter out = Writers.newPrintWriter(impliedLargeDot)) {
                dotProducer.writeAllRelationships(database, tables, false, showDetailedTables, stats, out, outputDir);
            }
            MustacheTableDiagram impliedLargeDiagram = mustacheDiagramFactory.generateSummaryDiagram("Large Implied", impliedLargeDot, dotBaseFilespec + ".implied.large");
            impliedLargeDiagram.setIsImplied(true);
            diagrams.add(impliedLargeDiagram);
        }
        if (!diagrams.isEmpty()) {
            diagrams.get(0).setActive(true);
        }
        return new MustacheSummaryDiagramResults(diagrams, stats, hasRealRelationships, impliedConstraints);
    }
}