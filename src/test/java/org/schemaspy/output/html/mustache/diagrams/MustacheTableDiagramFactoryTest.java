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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.stubbing.Answer;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.view.MustacheTableDiagram;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MustacheTableDiagramFactoryTest {

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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setupTable() {
        when(table.getName()).thenReturn("table");
        when(table.getMaxChildren()).thenReturn(0);
        when(table.getMaxParents()).thenReturn(0);
        when(table.isView()).thenReturn(false);
    }

    @Test
    public void onlyOneDiagram() throws IOException {
        File outputDir = temporaryFolder.newFolder("onediagram");
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

        DiagramFactory diagramFactory = mock(DiagramFactory.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResults.class));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(1);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
    }

    @Test
    public void onlyTwoDiagram() throws IOException {
        File outputDir = temporaryFolder.newFolder("twodiagrams");
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

        DiagramFactory diagramFactory = mock(DiagramFactory.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResults.class));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(2);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isFalse();
    }

    @Test
    public void threeDiagramsOneIsImplied() throws IOException {
        File outputDir = temporaryFolder.newFolder("threediagrams");
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

        DiagramFactory diagramFactory = mock(DiagramFactory.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResults.class));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, mustacheDiagramFactory, outputDir, 2);
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
    public void fourDiagramsTwoAreImplied() throws IOException {
        File outputDir = temporaryFolder.newFolder("fourdiagrams");
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

        DiagramFactory diagramFactory = mock(DiagramFactory.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResults.class));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, mustacheDiagramFactory, outputDir, 2);
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
    public void twoDiagramOnly1stDegree() throws IOException {
        File outputDir = temporaryFolder.newFolder("fourdiagrams1degree");
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

        DiagramFactory diagramFactory = mock(DiagramFactory.class);
        when(diagramFactory.generateTableDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResults.class));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, diagramFactory, mustacheDiagramFactory, outputDir, 1);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table);
        assertThat(mustacheTableDiagramList).hasSize(2);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(0).isImplied()).isFalse();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(1).isImplied()).isTrue();
    }

}