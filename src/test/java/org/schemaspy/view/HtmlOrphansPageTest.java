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
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlOrphansPageTest {

    @Test
    public void showError() {
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        when(htmlConfig.isOneOfMultipleSchemas()).thenReturn(false);
        when(htmlConfig.isPaginationEnabled()).thenReturn(true);
        DataTableConfig dataTableConfig = new DataTableConfig(htmlConfig, new CommandLineArguments());
        MustacheCompiler mustacheCompiler = new DefaultMustacheCompiler("errorInOrpahns", htmlConfig, dataTableConfig);
        HtmlOrphansPage htmlOrphansPage = new HtmlOrphansPage(mustacheCompiler);
        StringWriter writer = new StringWriter();
        htmlOrphansPage.write(Collections.emptyList(), false, writer);
        assertThat(writer.toString()).contains("Not all diagrams were created");
    }

}