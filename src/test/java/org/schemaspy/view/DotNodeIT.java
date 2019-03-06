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
package org.schemaspy.view;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.output.diagram.DiagramFactory;
import org.schemaspy.output.diagram.graphviz.GraphvizConfig;
import org.schemaspy.output.diagram.graphviz.GraphvizDot;

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

    private static DotFormatter dotFormatter = DotFormatter.getInstance();
    private static DiagramFactory diagramFactory;

    @BeforeClass
    public static void setup() throws IOException {
        GraphvizConfig graphvizConfig = mock(GraphvizConfig.class);
        when(graphvizConfig.isHighQuality()).thenReturn(true);
        when(graphvizConfig.getImageFormat()).thenReturn("png");
        diagramFactory = new DiagramFactory(new GraphvizDot(graphvizConfig), temporaryFolder.newFolder());
    }

    @Test
    public void illegalCharInTableName() throws IOException {
        Table table = mock(Table.class);
        when(table.getName()).thenReturn("<>&");

        File dotFile = temporaryFolder.newFile("dotFileTableName");
        Writer writer = Files.newBufferedWriter(dotFile.toPath(),StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        PrintWriter printWriter = new PrintWriter(writer);
        dotFormatter.writeOrphan(table, printWriter, "test");
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

        File dotFile = temporaryFolder.newFile("dotFileColumnName");
        Writer writer = Files.newBufferedWriter(dotFile.toPath(),StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        PrintWriter printWriter = new PrintWriter(writer);
        dotFormatter.writeOrphan(table, printWriter, "test");
        assertThatCode(() -> diagramFactory.generateOrphanDiagram(dotFile,"illegalColumnName"))
                .doesNotThrowAnyException();
    }

}