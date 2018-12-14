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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.stubbing.Answer;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.view.DotFormatter;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustacheTableDiagramFactoryTest {

    private static Table table = mock(Table.class);

    private static final Answer FIRST_DOT = invocation -> {
        Table table1 = invocation.getArgument(0);
        WriteStats writeStats1 = invocation.getArgument(2);
        writeStats1.wroteTable(table1);
        return Collections.emptySet();
    };
    private static final Answer SECOND_DOT = invocation -> {
        Table table1 = invocation.getArgument(0);
        WriteStats writeStats1 = invocation.getArgument(2);
        writeStats1.wroteTable(table1);
        writeStats1.wroteTable(table1);
        return Collections.emptySet();
    };
    private static final Answer SECOND_DOT_WITH_IMPLIED = invocation -> {
        Table table1 = invocation.getArgument(0);
        WriteStats writeStats1 = invocation.getArgument(2);
        writeStats1.wroteTable(table1);
        writeStats1.wroteTable(table1);
        ImpliedForeignKeyConstraint impliedForeignKeyConstraint = mock(ImpliedForeignKeyConstraint.class);
        return Collections.singleton(impliedForeignKeyConstraint);
    };

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setupTable() {
        when(table.getMaxChildren()).thenReturn(1);
        when(table.getMaxParents()).thenReturn(1);
        when(table.isView()).thenReturn(false);
    }

    @Test
    public void noDiagrams() throws IOException {
        WriteStats writeStats = new WriteStats(Collections.emptyList());
        Table tableNoRelationships = mock(Table.class);
        when(tableNoRelationships.getMaxChildren()).thenReturn(0);
        when(tableNoRelationships.getMaxParents()).thenReturn(0);

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(null, null, temporaryFolder.newFolder("nodiagrams"), 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(tableNoRelationships, writeStats);
        assertThat(mustacheTableDiagramList).isEmpty();
    }

    @Test
    public void onlyOneDiagram() throws IOException {
        File outputDir = temporaryFolder.newFolder("onediagram");
        WriteStats writeStats = new WriteStats(Collections.emptyList());

        DotFormatter dotProducer = mock(DotFormatter.class);
        when(dotProducer.writeRealRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(FIRST_DOT);
        when(dotProducer.writeRealRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(FIRST_DOT);

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateTableDiagram(anyString(),any(File.class),anyString())).thenReturn(new MustacheTableDiagram());

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table, writeStats);
        assertThat(mustacheTableDiagramList.size()).isEqualTo(1);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
    }

    @Test
    public void onlyTwoDiagram() throws IOException {
        File outputDir = temporaryFolder.newFolder("twodiagrams");
        WriteStats writeStats = new WriteStats(Collections.emptyList());

        DotFormatter dotProducer = mock(DotFormatter.class);
        when(dotProducer.writeRealRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(FIRST_DOT);
        when(dotProducer.writeRealRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(SECOND_DOT);

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateTableDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table, writeStats);
        assertThat(mustacheTableDiagramList.size()).isEqualTo(2);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
    }

    @Test
    public void threeDiagramsOneIsImplied() throws IOException {
        File outputDir = temporaryFolder.newFolder("threediagrams");
        WriteStats writeStats = new WriteStats(Collections.emptyList());

        DotFormatter dotProducer = mock(DotFormatter.class);
        when(dotProducer.writeRealRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(FIRST_DOT);
        when(dotProducer.writeRealRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(SECOND_DOT_WITH_IMPLIED);
        doAnswer(invocation -> {
            FIRST_DOT.answer(invocation);
            return null;
        }).
        when(dotProducer).writeAllRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir));
        doAnswer(invocation -> {
            FIRST_DOT.answer(invocation);
            return null;
        }).
                when(dotProducer).writeAllRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir));

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateTableDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table, writeStats);
        assertThat(mustacheTableDiagramList.size()).isEqualTo(3);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).isImplied()).isTrue();
    }

    @Test
    public void fourDiagramsTwoIsImplied() throws IOException {
        File outputDir = temporaryFolder.newFolder("fourdiagrams");
        WriteStats writeStats = new WriteStats(Collections.emptyList());

        DotFormatter dotProducer = mock(DotFormatter.class);
        when(dotProducer.writeRealRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(FIRST_DOT);
        when(dotProducer.writeRealRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir)))
                .then(SECOND_DOT_WITH_IMPLIED);
        doAnswer(invocation -> {
            FIRST_DOT.answer(invocation);
            return null;
        }).
                when(dotProducer).writeAllRelationships(eq(table), eq(false), any(WriteStats.class), any(PrintWriter.class), eq(outputDir));
        doAnswer(invocation -> {
            SECOND_DOT.answer(invocation);
            return null;
        }).
                when(dotProducer).writeAllRelationships(eq(table), eq(true), any(WriteStats.class), any(PrintWriter.class), eq(outputDir));

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateTableDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());

        MustacheTableDiagramFactory mustacheTableDiagramFactory = new MustacheTableDiagramFactory(dotProducer, mustacheDiagramFactory, outputDir, 2);
        List<MustacheTableDiagram> mustacheTableDiagramList = mustacheTableDiagramFactory.generateTableDiagrams(table, writeStats);
        assertThat(mustacheTableDiagramList.size()).isEqualTo(4);
        assertThat(mustacheTableDiagramList.get(0).getActive()).isNotEmpty();
        assertThat(mustacheTableDiagramList.get(1).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(2).isImplied()).isTrue();
        assertThat(mustacheTableDiagramList.get(3).getActive()).isNullOrEmpty();
        assertThat(mustacheTableDiagramList.get(3).isImplied()).isTrue();
    }

}