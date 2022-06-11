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

import org.junit.jupiter.api.Test;
import org.schemaspy.output.html.HtmlException;
import org.schemaspy.testing.TestMustacheCompiler;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class HtmlOrphansPageTest {

    @Test
    void orphanPageUsesCorrectTemplateName() {
        TestMustacheCompiler mustacheCompiler = new TestMustacheCompiler();
        new HtmlOrphansPage(mustacheCompiler, () -> "").write(null);
        assertThat(mustacheCompiler.pageData().getTemplateName()).isEqualTo("orphans.html");
    }

    @Test
    void orphanPageIncludesDiagram() {
        TestMustacheCompiler mustacheCompiler = new TestMustacheCompiler();

        new HtmlOrphansPage(mustacheCompiler, () -> "yes it does").write(null);
        assertThat(mustacheCompiler.pageData().getScope()).containsEntry("diagram", "yes it does");
    }

    @Test
    void throwsHtmlExceptionInsteadOfIOException() throws IOException {
        MustacheCompiler mustacheCompiler = mock(MustacheCompiler.class);

        doThrow(new IOException("failed")).when(mustacheCompiler).write(any(PageData.class), any());
        HtmlOrphansPage htmlOrphansPage =new HtmlOrphansPage(mustacheCompiler, () -> "yes it does");
        assertThatThrownBy(() ->
                htmlOrphansPage.write(null)
        ).isInstanceOf(HtmlException.class).hasCauseInstanceOf(IOException.class);
    }
}