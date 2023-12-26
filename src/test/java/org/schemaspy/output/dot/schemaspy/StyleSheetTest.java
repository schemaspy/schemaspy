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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleSheetTest {

    private static final StyleSheet styleSheet = new StyleSheet("layout", "schemaSpy.css").load();

    @Test
    void canGetBodyBackground() {
        assertThat(styleSheet.getBodyBackground()).isEqualTo("#ffffff");
    }

    @Test
    void canGetTableBackground() {
        assertThat(styleSheet.getTableBackground()).isEqualTo("#ffffff");
    }

    @Test
    void canGetTableHeadBackground() {
        assertThat(styleSheet.getTableHeadBackground()).isEqualTo("#f5f5f5");
    }

    @Test
    void canGetIndexedColumnBackground() {
        assertThat(styleSheet.getIndexedColumnBackground()).isEqualTo("#ffffff");
    }

    @Test
    void canGetExcludedColumnBackgroundColor() {
        assertThat(styleSheet.getExcludedColumnBackgroundColor()).isEqualTo("#c0c0c0");
    }

}