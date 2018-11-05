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
package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlTest {

    private static final Html htmlEncoder = new Html();

    @Test
    public void willEscapeLesserThan() {
        String input = "<hello<";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&lt;hello&lt;");
    }

    @Test
    public void willEscapeGreaterThan() {
        String input = ">hello>";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&gt;hello&gt;");
    }

    @Test
    public void willEscapeQuotation() {
        String input = "\"hello\"";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&quot;hello&quot;");
    }

    @Test
    public void willEscapeApostropheQuote() {
        String input = "'hello'";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&apos;hello&apos;");
    }

    @Test
    public void willAmpersandQuote() {
        String input = "'hello'";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&apos;hello&apos;");
    }

    @Test
    public void willEscapeEverything() {
        String input = "'><&\"hello<<<\"&&>'";
        String output = htmlEncoder.escape(input);
        assertThat(output).isEqualTo("&apos;&gt;&lt;&amp;&quot;hello&lt;&lt;&lt;&quot;&amp;&amp;&gt;&apos;");
    }

}