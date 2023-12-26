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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.stubbing.Answer;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramResult;
import org.schemaspy.output.diagram.TableDiagram;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.view.MustacheTableDiagram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MustacheTableDiagramFactoryTest {

    private static Table table = mock(Table.class);

    private static final Answer ONE_WRITTEN = invocation -> {
        Table table1 = invocation.getArgument(0);
        LongAdder writeStats1 = invocation.getArgument(2);
        writeStats1.add(1);
        return Collections.emptySet();
    };
    private static final Answer TWO_WRITTEN = invocation -> {
        Table table1 = invocation.getArgument(0);
        LongAdder writeStats1 = invocation.getArgument(2);
        writeStats1.add(2);
        return Collections.emptySet();
    };

    @TempDir
    Path temporaryFolder;

    @BeforeAll
    static void setupTable() {
        when(table.getName()).thenReturn("table");
        when(table.getMaxChildren()).thenReturn(0);
        when(table.getMaxParents()).thenReturn(0);
        when(table.isView()).thenReturn(false);
    }

    @Test
    void onlyOneDiagram() throws IOException {
        File outputDir = temporaryFolder.resolve("onediagram").toFile();
        when(table.hasImpliedConstraints(1)).thenReturn(false);
        when(table.hasImpliedConstraints(2)).thenReturn(false);

        DotFormatter dotProducer = mock(DotFormatter.class);
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));

        TableDiagram diagramFactory = mock(TableDiagram.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(1);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
    }

    @Test
    void onlyTwoDiagram() throws IOException {
        File outputDir = temporaryFolder.resolve("twodiagrams").toFile();
        when(table.hasImpliedConstraints(1)).thenReturn(false);
        when(table.hasImpliedConstraints(2)).thenReturn(false);

        DotFormatter dotProducer = mock(DotFormatter.class);
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            TWO_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));

        TableDiagram diagramFactory = mock(TableDiagram.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(2);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isFalse();
    }

    @Test
    void threeDiagramsOneIsImplied() throws IOException {
        File outputDir = temporaryFolder.resolve("threediagrams").toFile();
        when(table.hasImpliedConstraints(1)).thenReturn(true);
        when(table.hasImpliedConstraints(2)).thenReturn(true);

        DotFormatter dotProducer = mock(DotFormatter.class);
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableAllRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            TWO_WRITTEN.answer(invocation);
            return null;
        }).
                when(dotProducer).writeTableAllRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));

        TableDiagram diagramFactory = mock(TableDiagram.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(3);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isTrue();
        assertThat(mustacheTableDiagramList.get(2).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).isImplied()).isTrue();
    }

    @Test
    void fourDiagramsTwoAreImplied() throws IOException {
        File outputDir = temporaryFolder.resolve("fourdiagrams").toFile();
        when(table.hasImpliedConstraints(1)).thenReturn(true);
        when(table.hasImpliedConstraints(2)).thenReturn(true);

        DotFormatter dotProducer = mock(DotFormatter.class);
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            TWO_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).
                when(dotProducer).writeTableAllRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            TWO_WRITTEN.answer(invocation);
            return null;
        }).
                when(dotProducer).writeTableAllRelationships(eq(table), eq(true), any(LongAdder.class), any(PrintWriter.class));

        TableDiagram diagramFactory = mock(TableDiagram.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(4);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(2).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).isImplied()).isTrue();
        assertThat(mustacheTableDiagramList.get(3).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(3).isImplied()).isTrue();
    }

    @Test
    void twoDiagramOnly1stDegree() throws IOException {
        File outputDir = temporaryFolder.resolve("fourdiagrams1degree").toFile();
        when(table.hasImpliedConstraints(1)).thenReturn(true);
        when(table.hasImpliedConstraints(2)).thenReturn(true);

        DotFormatter dotProducer = mock(DotFormatter.class);
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableRealRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));
        doAnswer(invocation -> {
            ONE_WRITTEN.answer(invocation);
            return null;
        }).when(dotProducer).writeTableAllRelationships(eq(table), eq(false), any(LongAdder.class), any(PrintWriter.class));

        TableDiagram diagramFactory = mock(TableDiagram.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, outputDir, 1);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(2);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isTrue();
    }

}