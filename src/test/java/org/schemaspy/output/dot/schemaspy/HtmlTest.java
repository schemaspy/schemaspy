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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlTest {

    @ParameterizedTest
    @CsvSource(
            quoteCharacter = '*',
            useHeadersInDisplayName = true,
            textBlock = """
                    input,                  output
                    <hello<,                &lt;hello&lt;
                    >hello>,                &gt;hello&gt;
                    "hello",                &quot;hello&quot;
                    'hello',                &apos;hello&apos;
                    '><&"hello<<<"&&>',   &apos;&gt;&lt;&amp;&quot;hello&lt;&lt;&lt;&quot;&amp;&amp;&gt;&apos;
                    """
    )
    void htmlEscape(String input, String output) {
        assertThat(new Html().escape(input)).isEqualTo(output);
    }
}