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

import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.util.Writers;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Nils Petzaell
 */
public class MustacheTableDiagramFactory {

    private final DotFormatter dotProducer;
    private final MustacheDiagramFactory mustacheDiagramFactory;
    private final File tableDir;
    private final int degreeOfSeparation;

    public MustacheTableDiagramFactory(DotFormatter dotProducer, MustacheDiagramFactory mustacheDiagramFactory, File outputDir, int degreeOfSeparation) {
        this.dotProducer = dotProducer;
        this.mustacheDiagramFactory = mustacheDiagramFactory;
        this.tableDir = outputDir.toPath().resolve("diagrams").resolve("tables").toFile();
        tableDir.mkdirs();
        this.degreeOfSeparation = degreeOfSeparation;
    }

    public List<MustacheTableDiagram> generateTableDiagrams(Table table, WriteStats stats) throws IOException {
        List<MustacheTableDiagram> diagrams = new ArrayList<>();

        File oneDegreeDotFile = new File(tableDir, table.getName() + ".1degree.dot");
        File twoDegreesDotFile = new File(tableDir, table.getName() + ".2degrees.dot");
        File oneImpliedDotFile = new File(tableDir, table.getName() + ".implied1degrees.dot");
        File twoImpliedDotFile = new File(tableDir, table.getName() + ".implied2degrees.dot");

        // delete before we start because we'll use the existence of these files to determine
        // if they should be turned into pngs & presented
        Files.deleteIfExists(oneDegreeDotFile.toPath());
        Files.deleteIfExists(twoDegreesDotFile.toPath());
        Files.deleteIfExists(oneImpliedDotFile.toPath());
        Files.deleteIfExists(twoImpliedDotFile.toPath());

        if (hasNoRelationships(table)) {
            return diagrams;
        }

        Set<ForeignKeyConstraint> impliedConstraints;

        WriteStats oneStats = new WriteStats(stats);
        try (PrintWriter dotOut = Writers.newPrintWriter(oneDegreeDotFile)) {
            impliedConstraints = dotProducer.writeRealRelationships(table, false, oneStats, dotOut);
        }
        MustacheTableDiagram oneDiagram = mustacheDiagramFactory.generateTableDiagram("One", oneDegreeDotFile, table.getName() + ".1degree");
        oneDiagram.setActive(true);
        diagrams.add(oneDiagram);

        if (degreeOfSeparation == 2) {
            WriteStats twoStats = new WriteStats(stats);
            try (PrintWriter dotOut = Writers.newPrintWriter(twoDegreesDotFile)) {
                impliedConstraints = dotProducer.writeRealRelationships(table, true, twoStats, dotOut);
            }

            if (sameWritten(oneStats, twoStats)) {
                Files.deleteIfExists(twoDegreesDotFile.toPath()); // no different than before, so don't show it
            } else {
                diagrams.add(mustacheDiagramFactory.generateTableDiagram("Two degrees", twoDegreesDotFile, table.getName() + ".2degrees"));
            }
        }

        if (notEmpty(impliedConstraints)) {
            WriteStats oneImplied = new WriteStats(stats);
            try (PrintWriter dotOut = Writers.newPrintWriter(oneImpliedDotFile)) {
                dotProducer.writeAllRelationships(table, false, oneImplied, dotOut);
            }
            MustacheTableDiagram oneImpliedDiagram = mustacheDiagramFactory.generateTableDiagram("One implied", oneImpliedDotFile, table.getName() + ".implied1degrees");
            oneImpliedDiagram.setIsImplied(true);
            diagrams.add(oneImpliedDiagram);

            if (degreeOfSeparation == 2) {
                WriteStats twoImplied = new WriteStats(stats);
                try (PrintWriter dotOut = Writers.newPrintWriter(twoImpliedDotFile)) {
                    dotProducer.writeAllRelationships(table, true, twoImplied, dotOut);
                }
                if (sameWritten(oneImplied, twoImplied)) {
                    Files.deleteIfExists(twoImpliedDotFile.toPath());
                } else {
                    MustacheTableDiagram twoImpliedDiagram = mustacheDiagramFactory.generateTableDiagram("Two implied", twoImpliedDotFile, table.getName() + ".implied2degrees");
                    twoImpliedDiagram.setIsImplied(true);
                    diagrams.add(twoImpliedDiagram);
                }
            }
        }


        return diagrams;
    }

    private static boolean hasNoRelationships(Table table) {
        return table.getMaxChildren() + table.getMaxParents() < 1;
    }

    private static boolean notEmpty(Collection<?> collection) {
        return !collection.isEmpty();
    }

    private static boolean sameWritten(WriteStats first, WriteStats second) {
        return first.getNumTablesWritten() + first.getNumViewsWritten() == second.getNumTablesWritten() + second.getNumViewsWritten();
    }

}