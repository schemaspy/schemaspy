/*
 * Copyright (C) 2020 Nils Petzaell
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
package org.schemaspy.util.naming;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FileNameGeneratorTest {

    @Test
    void wontGenerateSameNameForTwoDifferentNames() {
        String nameOne = "98765432109876543210987654321098765432109876543210";
        String nameTwo = "12345678901234567890123456789012345678901234567890";
        assertThat(new FileNameGenerator(nameOne).value()).isNotEqualToIgnoringCase(new FileNameGenerator(nameTwo).value());
    }

    @Test
    void wontGenerateSameForSimilar() {
        String nameOne = "Test\tif/name/is#fixed or not";
        String nameTwo = "Test\tif\tname/is#fixed or not";
        assertThat(new FileNameGenerator(nameOne).value()).isNotEqualToIgnoringCase(new FileNameGenerator(nameTwo).value());
    }

    @ParameterizedTest(name = "{0}, \"{1}\" should become \"{2}\"")
    @CsvSource(
        textBlock = """
            #case,         input,                                      output
            japanese,      こんにちは,                                   ______bfca3f39
            illegal,       Test\tif/name/is#fixed or not,              Test_if_name_is_fixed_or_not_f9e4eeb2
            shortAndOk,    1234567890123456789012345678901234567890,   1234567890123456789012345678901234567890
            short40C,      12345678901234567890123456789012345678901,  1234567890123456789012345678901_6e3e05c5
            withDot,       file.name,                                  file.name
            """
    )
    void generateName(String description, String input, String output) {
        assertThat(new FileNameGenerator(input).value()).isEqualTo(output).as("Failed %s", description);
    }
}