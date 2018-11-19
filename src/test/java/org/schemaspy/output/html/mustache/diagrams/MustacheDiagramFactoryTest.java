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
import org.junit.Test;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.DiagramResults;
import org.schemaspy.view.MustacheTableDiagram;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MustacheDiagramFactoryTest {

    private static final DiagramFactory DIAGRAM_FACTORY = mock(DiagramFactory.class);
    private static final MustacheDiagramFactory mustacheDiagramFactory = new MustacheDiagramFactory(DIAGRAM_FACTORY);

    @BeforeClass
    public static void setupOrphan() {
        File orphan = mock(File.class);
        when(orphan.getName()).thenReturn("orphan.png");
        DiagramResults results = new DiagramResults(orphan,"<map name=\"orphanMap\">","png");
        when(DIAGRAM_FACTORY.generateOrphanDiagram(any(File.class),anyString())).thenReturn(results);
    }

    @BeforeClass
    public static void setupTable() {
        File orphan = mock(File.class);
        when(orphan.getName()).thenReturn("table.png");
        DiagramResults results = new DiagramResults(orphan,"<map name=\"tableMap\">","png");
        when(DIAGRAM_FACTORY.generateTableDiagram(any(File.class),anyString())).thenReturn(results);
    }

    @BeforeClass
    public static void setupSummary() {
        File orphan = mock(File.class);
        when(orphan.getName()).thenReturn("summary.png");
        DiagramResults results = new DiagramResults(orphan,"<map name=\"summaryMap\">", "png");
        when(DIAGRAM_FACTORY.generateSummaryDiagram(any(File.class),anyString())).thenReturn(results);
    }

    @Test
    public void generateOrphanDiagram() {
        MustacheTableDiagram mustacheTableDiagram = mustacheDiagramFactory.generateOrphanDiagram("Orphan 1", mock(File.class), "orphan");
        assertThat(mustacheTableDiagram.getName()).isEqualTo("Orphan 1");
        assertThat(mustacheTableDiagram.getId()).isEqualTo("orphan1DegreeImg");
        assertThat(mustacheTableDiagram.getFileName()).isEqualTo("orphan.png");
        assertThat(mustacheTableDiagram.getMapName()).isEqualTo("orphanMap");
        assertThat(mustacheTableDiagram.getMap()).isEqualTo("<map name=\"orphanMap\">");
    }

    @Test
    public void generateTableDiagram() {
        MustacheTableDiagram mustacheTableDiagram = mustacheDiagramFactory.generateTableDiagram("Table 1", mock(File.class), "table");
        assertThat(mustacheTableDiagram.getName()).isEqualTo("Table 1");
        assertThat(mustacheTableDiagram.getId()).isEqualTo("table1DegreeImg");
        assertThat(mustacheTableDiagram.getFileName()).isEqualTo("table.png");
        assertThat(mustacheTableDiagram.getMapName()).isEqualTo("tableMap");
        assertThat(mustacheTableDiagram.getMap()).isEqualTo("<map name=\"tableMap\">");
    }

    @Test
    public void generateSummaryDiagram() {
        MustacheTableDiagram mustacheTableDiagram = mustacheDiagramFactory.generateSummaryDiagram("Summary 1", mock(File.class), "summary");
        assertThat(mustacheTableDiagram.getName()).isEqualTo("Summary 1");
        assertThat(mustacheTableDiagram.getId()).isEqualTo("summary1DegreeImg");
        assertThat(mustacheTableDiagram.getFileName()).isEqualTo("summary.png");
        assertThat(mustacheTableDiagram.getMapName()).isEqualTo("summaryMap");
        assertThat(mustacheTableDiagram.getMap()).isEqualTo("<map name=\"summaryMap\">");
    }
}