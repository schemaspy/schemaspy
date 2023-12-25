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
package org.schemaspy.input.dbms.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DurationFormatterTest {

    @Test
    void lessThenOneSecond() {
        String formatted = DurationFormatter.formatMS(899);
        assertThat(formatted).isEqualTo("899 ms");
    }

    @Test
    void lessThenOneMinute() {
        String formatted = DurationFormatter.formatMS(12345);
        assertThat(formatted).isEqualTo("12 s 345 ms");
    }

    @Test
    void lessThenOneHour() {
        String formatted = DurationFormatter.formatMS(123456);
        assertThat(formatted).isEqualTo("2 min 3 s 456 ms");
    }

    @Test
    void moreThanOneHour() {
        String formatted = DurationFormatter.formatMS(12345678);
        assertThat(formatted).isEqualTo("3 hr 25 min 45 s 678 ms");
    }

    @Test
    void exactlyOneMinute() {
        String formatted = DurationFormatter.formatMS(60000);
        assertThat(formatted).isEqualTo("1 min");
    }

}