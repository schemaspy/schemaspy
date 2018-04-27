/*
 * Copyright (C) 2017 Daniel Watt
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
public class HtmlFormatterTest {

    @Test
    public void escapeHtml() {
        assertThat(HtmlFormatter.escapeHtml("string")).isEqualTo("string");
        assertThat(HtmlFormatter.escapeHtml("string with spaces")).isEqualTo("string with spaces");
    }

    @Test
    public void urlEncodeLink() {
        assertThat(HtmlFormatter.urlEncodeLink("string")).isEqualTo("string");
        assertThat(HtmlFormatter.urlEncodeLink("string with spaces")).isEqualTo("string%20with%20spaces");
    }
}