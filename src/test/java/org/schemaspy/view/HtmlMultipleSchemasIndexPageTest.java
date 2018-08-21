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
import org.schemaspy.model.Catalog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlMultipleSchemasIndexPageTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void multiMainIndexShouldHaveDescription() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File outputDir = temporaryFolder.newFolder();
        Files.createDirectories(outputDir.toPath());
        HtmlMultipleSchemasIndexPage.getInstance().write(outputDir, "justADb", new MustacheCatalog(new Catalog("dbo"),""), Collections.emptyList(),"A Description", "JAVA_TEST 1.0");
        String content = new String(Files.readAllBytes(outputDir.toPath().resolve("index.html")), StandardCharsets.UTF_8);
        assertThat(content).contains("<p>A Description</p>");
    }

    @Test
    public void multiMainIndexShouldNOTHaveDescription() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        File outputDir = temporaryFolder.newFolder();
        Files.createDirectories(outputDir.toPath());
        HtmlMultipleSchemasIndexPage.getInstance().write(outputDir, "justADb", new MustacheCatalog(new Catalog("dbo"),""), Collections.emptyList(),null, "JAVA_TEST 1.0");
        String content = new String(Files.readAllBytes(outputDir.toPath().resolve("index.html")), StandardCharsets.UTF_8);
        assertThat(content).doesNotContain("<p>A Description</p>");
    }

}