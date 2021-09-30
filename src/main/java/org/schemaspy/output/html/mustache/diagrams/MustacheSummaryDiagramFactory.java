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
import org.schemaspy.output.OutputException;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.util.Writers;
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

/**
 * @author Nils Petzaell
 */
public class MustacheSummaryDiagramFactory {

    private static final String FILE_PREFIX = "relationships";
    private static final String FAILED_DOT = "Failed to produce dot: ";
    private static final String FAILED_DIAGRAM = "Failed to produce diagram for: ";

    private final DotFormatter dotProducer;
    private final MustacheDiagramFactory mustacheDiagramFactory;
    private final ImpliedConstraintsFinder impliedConstraintsFinder;
    private final Path summaryDir;

    public MustacheSummaryDiagramFactory(DotFormatter dotProducer, MustacheDiagramFactory mustacheDiagramFactory, ImpliedConstraintsFinder impliedConstraintsFinder, File outputDir) {
        this.dotProducer = dotProducer;
        this.mustacheDiagramFactory = mustacheDiagramFactory;
        this.impliedConstraintsFinder = impliedConstraintsFinder;
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
        List<OutputException> outputExceptions = new ArrayList<>();
        // generate the compact form of the relationships .dot file

        WriteStats stats = new WriteStats(tables);
        File realCompactDot = summaryDir.resolve(FILE_PREFIX + ".real.compact.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(realCompactDot)) {
            dotProducer.writeSummaryRealRelationships(database, tables, true, showDetailedTables, stats, out);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + realCompactDot.toString(), ioexception));
        }
        boolean hasRealRelationships = stats.getNumTablesWritten() > 0 || stats.getNumViewsWritten() > 0;


        if (hasRealRelationships) {
            try {
                MustacheTableDiagram realCompactDiagram = mustacheDiagramFactory.generateSummaryDiagram("Compact", realCompactDot, FILE_PREFIX + ".real.compact");
                realCompactDiagram.setActive(true);
                diagrams.add(realCompactDiagram);
            } catch (DiagramException diagramException) {
                outputExceptions.add(new OutputException(FAILED_DIAGRAM + realCompactDot.toString(), diagramException));
            }
            // real relationships exist so generate the 'big' form of the relationships .dot file
            generateRealLarge(database, tables, showDetailedTables, diagrams, outputExceptions, stats);
            generateRealFull(database, tables, showDetailedTables, diagrams, outputExceptions, stats);
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
            generateImpliedCompact(database, tables, showDetailedTables, diagrams, outputExceptions, stats);
            generateImpliedLarge(database, tables, showDetailedTables, diagrams, outputExceptions, stats);
        }
        if (!diagrams.isEmpty()) {
            diagrams.get(0).setActive(true);
        }
        return new MustacheSummaryDiagramResults(diagrams, stats, hasRealRelationships, impliedConstraints, outputExceptions);
    }

    private void generateRealLarge(Database database, Collection<Table> tables, boolean showDetailedTables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions, WriteStats stats) {
        File realLargeDot = summaryDir.resolve(FILE_PREFIX + ".real.large.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(realLargeDot)) {
            dotProducer.writeSummaryRealRelationships(database, tables, false, showDetailedTables, stats, out);
            MustacheTableDiagram realLargeDiagram = mustacheDiagramFactory.generateSummaryDiagram("Large", realLargeDot, FILE_PREFIX + ".real.large");
            diagrams.add(realLargeDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + realLargeDot.toString(), ioexception));
        } catch (DiagramException diagramException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + realLargeDot.toString(), diagramException));
        }
    }

    private void generateRealFull(Database database, Collection<Table> tables, boolean showDetailedTables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions, WriteStats stats) {
        File realFullDot = summaryDir.resolve(FILE_PREFIX + ".real.full.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(realFullDot)) {
            dotProducer.writeSummaryFullRelationships(database, tables, false, showDetailedTables, stats, out);
            MustacheTableDiagram realFullDiagram = mustacheDiagramFactory.generateSummaryDiagram("Full", realFullDot, FILE_PREFIX + ".real.full");
            diagrams.add(realFullDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + realFullDot.toString(), ioexception));
        } catch (DiagramException diagramException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + realFullDot.toString(), diagramException));
        }
    }

    private void generateImpliedCompact(Database database, Collection<Table> tables, boolean showDetailedTables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions, WriteStats stats) {
        File impliedCompactDot = summaryDir.resolve(FILE_PREFIX + ".implied.compact.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(impliedCompactDot)) {
            dotProducer.writeSummaryAllRelationships(database, tables, true, showDetailedTables, stats, out);
            MustacheTableDiagram impliedCompactDiagram = mustacheDiagramFactory.generateSummaryDiagram("Compact Implied", impliedCompactDot, FILE_PREFIX + ".implied.compact");
            impliedCompactDiagram.setIsImplied(true);
            diagrams.add(impliedCompactDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + impliedCompactDot.toString(), ioexception));
        } catch (DiagramException diagramException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + impliedCompactDot.toString(), diagramException));
        }
    }

    private void generateImpliedLarge(Database database, Collection<Table> tables, boolean showDetailedTables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions, WriteStats stats) {
        File impliedLargeDot = summaryDir.resolve(FILE_PREFIX + ".implied.large.dot").toFile();
        try (PrintWriter out = Writers.newPrintWriter(impliedLargeDot)) {
            dotProducer.writeSummaryAllRelationships(database, tables, false, showDetailedTables, stats, out);
            MustacheTableDiagram impliedLargeDiagram = mustacheDiagramFactory.generateSummaryDiagram("Large Implied", impliedLargeDot, FILE_PREFIX + ".implied.large");
            impliedLargeDiagram.setIsImplied(true);
            diagrams.add(impliedLargeDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + impliedLargeDot.toString(), ioexception));
        } catch (DiagramException diagramException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + impliedLargeDot.toString(), diagramException));
        }
    }
}
