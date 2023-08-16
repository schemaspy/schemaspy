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

import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.output.OutputException;
import org.schemaspy.output.diagram.DiagramResult;
import org.schemaspy.output.diagram.RenderException;
import org.schemaspy.output.diagram.SummaryDiagram;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.util.DefaultPrintWriter;
import org.schemaspy.view.MustacheTableDiagram;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Nils Petzaell
 */
public class MustacheSummaryDiagramFactory {

    private static final String FILE_PREFIX = "relationships";
    private static final String FAILED_DOT = "Failed to produce dot: ";
    private static final String FAILED_DIAGRAM = "Failed to produce diagram for: ";
    private final DotFormatter dotProducer;
    private final SummaryDiagram diagramFactory;
    private final boolean hasRealConstraints;
    private final boolean hasImpliedConstraints;
    private final Path summaryDir;
    private final ProgressListener progressListener;

    public MustacheSummaryDiagramFactory(
        DotFormatter dotProducer,
        SummaryDiagram diagramFactory,
        boolean hasRealConstraints,
        boolean hasImpliedConstraints,
        File outputDir,
        ProgressListener progressListener
    ) {
        this.dotProducer = dotProducer;
        this.diagramFactory = diagramFactory;
        this.hasRealConstraints = hasRealConstraints;
        this.hasImpliedConstraints = hasImpliedConstraints;
        this.summaryDir = outputDir.toPath().resolve("diagrams").resolve("summary");
        this.progressListener = progressListener;
    }

    public MustacheSummaryDiagramResults generateSummaryDiagrams(
            Database database,
            Collection<Table> tables
    ) throws IOException {
        if (tables.isEmpty()) {
            return new MustacheSummaryDiagramResults(Collections.emptyList(), Collections.emptyList());
        }
        List<MustacheTableDiagram> diagrams = new ArrayList<>();
        List<OutputException> outputExceptions = new ArrayList<>();
        Files.createDirectories(summaryDir);
        // generate the compact form of the relationships .dot file

        if (hasRealConstraints) {
            File realCompactDot = summaryDir.resolve(FILE_PREFIX + ".real.compact.dot").toFile();
            try (PrintWriter out = new DefaultPrintWriter(realCompactDot)) {
                dotProducer.writeSummaryRealRelationships(database, tables, true, out);
                DiagramResult results = diagramFactory.generateSummaryDiagram(realCompactDot, FILE_PREFIX + ".real.compact");
                MustacheTableDiagram realCompactDiagram = new MustacheTableDiagram("Compact", results, false);
                realCompactDiagram.setActive(true);
                diagrams.add(realCompactDiagram);
            } catch (IOException ioexception) {
                outputExceptions.add(new OutputException(FAILED_DOT + realCompactDot.toString(), ioexception));
            } catch (RenderException renderException) {
                outputExceptions.add(new OutputException(FAILED_DIAGRAM + realCompactDot.toString(), renderException));
            }
            // real relationships exist so generate the 'big' form of the relationships .dot file
            generateRealLarge(database, tables, diagrams, outputExceptions);
        }

        progressListener.createdSummary();
        if (hasImpliedConstraints) {
            generateImpliedCompact(database, tables, diagrams, outputExceptions);
            generateImpliedLarge(database, tables, diagrams, outputExceptions);
        }
        if (!diagrams.isEmpty()) {
            diagrams.get(0).setActive(true);
        }
        return new MustacheSummaryDiagramResults(diagrams, outputExceptions);
    }

    private void generateRealLarge(Database database, Collection<Table> tables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions) {
        File realLargeDot = summaryDir.resolve(FILE_PREFIX + ".real.large.dot").toFile();
        try (PrintWriter out = new DefaultPrintWriter(realLargeDot)) {
            dotProducer.writeSummaryRealRelationships(database, tables, false, out);
            DiagramResult results = diagramFactory.generateSummaryDiagram(realLargeDot, FILE_PREFIX + ".real.large");
            MustacheTableDiagram realLargeDiagram = new MustacheTableDiagram("Large", results, false);
            diagrams.add(realLargeDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + realLargeDot.toString(), ioexception));
        } catch (RenderException renderException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + realLargeDot.toString(), renderException));
        }
    }

    private void generateImpliedCompact(Database database, Collection<Table> tables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions) {
        File impliedCompactDot = summaryDir.resolve(FILE_PREFIX + ".implied.compact.dot").toFile();
        try (PrintWriter out = new DefaultPrintWriter(impliedCompactDot)) {
            dotProducer.writeSummaryAllRelationships(database, tables, true, out);
            DiagramResult results = diagramFactory.generateSummaryDiagram(impliedCompactDot, FILE_PREFIX + ".implied.compact");
            MustacheTableDiagram impliedCompactDiagram = new MustacheTableDiagram("Compact Implied", results, true);
            diagrams.add(impliedCompactDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + impliedCompactDot.toString(), ioexception));
        } catch (RenderException renderException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + impliedCompactDot.toString(), renderException));
        }
    }

    private void generateImpliedLarge(Database database, Collection<Table> tables, List<MustacheTableDiagram> diagrams, List<OutputException> outputExceptions) {
        File impliedLargeDot = summaryDir.resolve(FILE_PREFIX + ".implied.large.dot").toFile();
        try (PrintWriter out = new DefaultPrintWriter(impliedLargeDot)) {
            dotProducer.writeSummaryAllRelationships(database, tables, false, out);
            DiagramResult results = diagramFactory.generateSummaryDiagram(impliedLargeDot, FILE_PREFIX + ".implied.large");
            MustacheTableDiagram impliedLargeDiagram = new MustacheTableDiagram("Large Implied", results, true);
            diagrams.add(impliedLargeDiagram);
        } catch (IOException ioexception) {
            outputExceptions.add(new OutputException(FAILED_DOT + impliedLargeDot.toString(), ioexception));
        } catch (RenderException renderException) {
            outputExceptions.add(new OutputException(FAILED_DIAGRAM + impliedLargeDot.toString(), renderException));
        }
    }
}
