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

import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.util.Writers;
import org.schemaspy.view.FileNameGenerator;
import org.schemaspy.view.MustacheTableDiagram;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Nils Petzaell
 */
public class MustacheTableDiagramFactory {

    private final DotFormatter dotProducer;
    private final DiagramFactory diagramFactory;
    private final File tableDir;
    private final int degreeOfSeparation;

    public MustacheTableDiagramFactory(DotFormatter dotProducer, DiagramFactory diagramFactory, File outputDir, int degreeOfSeparation) {
        this.dotProducer = dotProducer;
        this.diagramFactory = diagramFactory;
        this.tableDir = outputDir.toPath().resolve("diagrams").resolve("tables").toFile();
        tableDir.mkdirs();
        this.degreeOfSeparation = degreeOfSeparation;
    }

    public List<MustacheTableDiagram> generateTableDiagrams(Table table) throws IOException {
        List<MustacheTableDiagram> diagrams = new ArrayList<>();
        diagrams.addAll(generateRealTableDiagrams(table));
        diagrams.addAll(generateImpliedTableDiagrams(table));
        return diagrams;
    }

    public List<MustacheTableDiagram> generateRealTableDiagrams(Table table) throws IOException {
        List<MustacheTableDiagram> diagrams = new ArrayList<>();

        String fileNameBase = new FileNameGenerator().generate(table.getName());

        File oneDegreeDotFile = new File(tableDir, fileNameBase + ".1degree.dot");
        File twoDegreesDotFile = new File(tableDir, fileNameBase + ".2degrees.dot");

        // delete before we start because we'll use the existence of these files to determine
        // if they should be turned into pngs & presented
        Files.deleteIfExists(oneDegreeDotFile.toPath());
        Files.deleteIfExists(twoDegreesDotFile.toPath());

        LongAdder oneStats = new LongAdder();
        try (PrintWriter dotOut = Writers.newPrintWriter(oneDegreeDotFile)) {
            dotProducer.writeTableRealRelationships(table, false, oneStats, dotOut);
        }
        DiagramResults results = diagramFactory.generateTableDiagram(oneDegreeDotFile, fileNameBase + ".1degree");
        MustacheTableDiagram oneDiagram = new MustacheTableDiagram("One", results);
        oneDiagram.setActive(true);
        diagrams.add(oneDiagram);

        if (degreeOfSeparation == 2) {
            LongAdder twoStats = new LongAdder();
            try (PrintWriter dotOut = Writers.newPrintWriter(twoDegreesDotFile)) {
                dotProducer.writeTableRealRelationships(table, true, twoStats, dotOut);
            }

            if (sameWritten(oneStats, twoStats)) {
                Files.deleteIfExists(twoDegreesDotFile.toPath()); // no different than before, so don't show it
            } else {
                DiagramResults resultsTwo = diagramFactory.generateTableDiagram(twoDegreesDotFile, fileNameBase + ".2degrees");
                MustacheTableDiagram twoDiagram = new MustacheTableDiagram("Two degrees", resultsTwo);
                diagrams.add(twoDiagram);
            }
        }

        return diagrams;
    }

    public List<MustacheTableDiagram> generateImpliedTableDiagrams(Table table) throws IOException {
        List<MustacheTableDiagram> diagrams = new ArrayList<>();

        String fileNameBase = new FileNameGenerator().generate(table.getName());

        File oneImpliedDotFile = new File(tableDir, fileNameBase + ".implied1degrees.dot");
        File twoImpliedDotFile = new File(tableDir, fileNameBase + ".implied2degrees.dot");

        // delete before we start because we'll use the existence of these files to determine
        // if they should be turned into pngs & presented
        Files.deleteIfExists(oneImpliedDotFile.toPath());
        Files.deleteIfExists(twoImpliedDotFile.toPath());

        if (table.hasImpliedConstraints(degreeOfSeparation)) {
            LongAdder oneImplied = new LongAdder();
            try (PrintWriter dotOut = Writers.newPrintWriter(oneImpliedDotFile)) {
                dotProducer.writeTableAllRelationships(table, false, oneImplied, dotOut);
            }

            DiagramResults results = diagramFactory.generateTableDiagram(oneImpliedDotFile, fileNameBase + ".implied1degrees");
            MustacheTableDiagram oneImpliedDiagram = new MustacheTableDiagram("One implied", results);
            oneImpliedDiagram.setIsImplied(true);
            diagrams.add(oneImpliedDiagram);

            if (degreeOfSeparation == 2) {
                LongAdder twoImplied = new LongAdder();
                try (PrintWriter dotOut = Writers.newPrintWriter(twoImpliedDotFile)) {
                    dotProducer.writeTableAllRelationships(table, true, twoImplied, dotOut);
                }
                if (sameWritten(oneImplied, twoImplied)) {
                    Files.deleteIfExists(twoImpliedDotFile.toPath());
                } else {
                    DiagramResults resultsTwo = diagramFactory.generateTableDiagram(twoImpliedDotFile, fileNameBase + ".implied2degrees");
                    MustacheTableDiagram twoImpliedDiagram = new MustacheTableDiagram("Two implied", resultsTwo);
                    twoImpliedDiagram.setIsImplied(true);
                    diagrams.add(twoImpliedDiagram);
                }
            }
        }


        return diagrams;
    }

    private static boolean sameWritten(LongAdder first, LongAdder second) {
        return first.sum() == second.sum();
    }

}