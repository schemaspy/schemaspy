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
package org.schemaspy.output.dot.schemaspy;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.Config;
import org.schemaspy.SimpleDotConfig;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.graphviz.GraphvizConfig;
import org.schemaspy.output.diagram.graphviz.GraphvizDot;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.schemaspy.graph.Orphan;
import org.schemaspy.output.dot.schemaspy.graph.Graph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DotNodeIT {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static DotConfig dotConfig =
            new SimpleDotConfig(
                    new DefaultFontConfig(
                            Config.getInstance().getFont(),
                            Config.getInstance().getFontSize()
                    ),
                    Config.getInstance().isRankDirBugEnabled(),
                    false,
                    Config.getInstance().isNumRowsEnabled(),
                    Config.getInstance().isOneOfMultipleSchemas()
            );
    private static DiagramFactory diagramFactory;

    private static File orphansDir;

    @BeforeClass
    public static void setup() throws IOException {
        GraphvizConfig graphvizConfig = mock(GraphvizConfig.class);
        when(graphvizConfig.isLowQuality()).thenReturn(false);
        when(graphvizConfig.getImageFormat()).thenReturn("png");
        orphansDir = temporaryFolder.getRoot().toPath().resolve("diagrams").resolve("orphans").toFile();
        diagramFactory = new DiagramFactory(new GraphvizDot(graphvizConfig), temporaryFolder.getRoot());
    }

    @Test
    public void illegalCharInTableName() throws IOException {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("<>&");
        File dotFile = new File(orphansDir, "dotFileColumnName");
        Writer writer = Files.newBufferedWriter(dotFile.toPath(),StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        PrintWriter printWriter = new PrintWriter(writer);
        Graph graph = new Orphan(
                table::getName,
                new DotConfigHeader(dotConfig, false),
                new DotNode(
                        table,
                        true,
                        new DotNodeConfig(true, true),
                        dotConfig
                )
        );
        printWriter.println(graph.dot());
        printWriter.flush();

        assertThatCode(() -> diagramFactory.generateOrphanDiagram(dotFile,"illegalTableName"))
                .doesNotThrowAnyException();
    }

    @Test
    public void illegalCharInColumnName() throws IOException {
        TableColumn tableColumn = mock(TableColumn.class);
        when(tableColumn.getName()).thenReturn("<>&");
        when(tableColumn.getShortTypeName()).thenReturn("ABC");
        when(tableColumn.getDetailedSize()).thenReturn("long");

        Table table = mock(Table.class);
        when(table.getName()).thenReturn("a");
        when(table.getColumns()).thenReturn(Collections.singletonList(tableColumn));

        File dotFile = new File(orphansDir, "dotFileColumnName");
        Writer writer = Files.newBufferedWriter(dotFile.toPath(),StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        PrintWriter printWriter = new PrintWriter(writer);
        Graph graph = new Orphan(
                table::getName,
                new DotConfigHeader(dotConfig, false),
                new DotNode(
                        table,
                        true,
                        new DotNodeConfig(true, true),
                        dotConfig
                )
        );
        printWriter.println(graph.dot());
        printWriter.flush();
        assertThatCode(() -> diagramFactory.generateOrphanDiagram(dotFile,"illegalColumnName"))
                .doesNotThrowAnyException();
    }

}