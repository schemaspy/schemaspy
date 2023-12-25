/*
 * Copyright (C) 2019 Nils Petzaell
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
package org.schemaspy.cli;

import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DegreeOfSeparationValidatorTest {

    private static String NAME = "-degree";
    private static final DegreeOfSeparationValidator VALIDATOR = new DegreeOfSeparationValidator();

    @Test
    void doesNotAllowZero() {
        assertThatExceptionOfType(ParameterException.class)
                .isThrownBy(() -> VALIDATOR.validate(NAME, 0));
    }

    @Test
    void doesAllowOne() {
        VALIDATOR.validate(NAME, 1);
    }

    @Test
    void doesAllowTwo() {
        VALIDATOR.validate(NAME, 2);
    }

    @Test
    void doesNotAllowThree() {
        assertThatExceptionOfType(ParameterException.class)
                .isThrownBy(() -> VALIDATOR.validate(NAME, 3));
    }
}