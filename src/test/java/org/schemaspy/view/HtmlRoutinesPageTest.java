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
import org.schemaspy.model.Routine;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlRoutinesPageTest {

    @Test
    public void markdownComment() {
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", htmlConfig);
        Config config = new Config();
        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler, config);
        Collection<Routine> routines = Collections.singletonList(new Routine("ARoutine", "Function", "Integer", "SQL", "SELECT 1", true, "IMMUTABLE", "INVOKER", "normal *emp* **strong**"));
        StringWriter actual = new StringWriter();

        htmlRoutinesPage.write(routines, actual);

        assertThat(actual.toString()).contains("<p>normal <em>emp</em> <strong>strong</strong></p>");
    }

}