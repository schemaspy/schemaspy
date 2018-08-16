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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlRoutinesPageTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void markdownComment() throws IOException {
        Database database = mock(Database.class);
        Collection<Routine> routines =Collections.singletonList(new Routine("ARoutine", "Function", "Integer", "SQL", "SELECT 1", true, "IMMUTABLE", "INVOKER", "normal *emp* **strong**"));
        when(database.getRoutines()).thenReturn(routines);
        File outputDir = temporaryFolder.newFolder();
        Files.createDirectory(outputDir.toPath().resolve("routines"));
        HtmlRoutinesPage.getInstance().write(database, outputDir);
        String routinesHtml = new String(Files.readAllBytes(Paths.get(outputDir.getPath(),"routines.html")), StandardCharsets.UTF_8);
        assertThat(routinesHtml).contains("<p>normal <em>emp</em> <strong>strong</strong></p>");
    }

}