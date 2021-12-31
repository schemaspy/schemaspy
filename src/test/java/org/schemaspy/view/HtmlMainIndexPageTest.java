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

import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.model.Catalog;
import org.schemaspy.model.Database;
import org.schemaspy.model.Schema;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlMainIndexPageTest {

    @Test
    public void descriptionSupportsMarkdown() {
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", htmlConfig);
        Config config = new Config("-desc", "\"normal *emp* **strong**\"");
        assertThat("normal *emp* **strong**").isEqualTo(config.getDescription());
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, config);
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).contains("<p>normal <em>emp</em> <strong>strong</strong></p>");
    }

    @Test
    public void noRowsTrue_RemovesRowsColumn() {
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        when(htmlConfig.isNumRowsEnabled()).thenReturn(false);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("noRowsTrue", htmlConfig);
        Config config = new Config();
        assertThat(Optional.empty()).isEqualTo(Optional.ofNullable(config.getDescription()));
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, config);
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).doesNotContain("<th align=\"right\" valign=\"bottom\">Rows</th>");
    }

    @Test
    public void noRowsFalse_HasRowsColumn() {
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        when(htmlConfig.isNumRowsEnabled()).thenReturn(true);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("noRowsFalse", htmlConfig);
        Config config = new Config();
        assertThat(Optional.empty()).isEqualTo(Optional.ofNullable(config.getDescription()));
        HtmlMainIndexPage htmlMainIndexPage = new HtmlMainIndexPage(mustacheCompiler, config);
        StringWriter writer = new StringWriter();
        Database database = mock(Database.class);
        when(database.getSchema()).thenReturn(new Schema("schema"));
        when(database.getCatalog()).thenReturn(new Catalog("catalog"));
        htmlMainIndexPage.write(database, Collections.emptyList(), Collections.emptyList(), writer);

        assertThat(writer.toString()).contains("<th align=\"right\" valign=\"bottom\">Rows</th>");
    }

}