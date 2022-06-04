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
package org.schemaspy.view;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileNameGeneratorTest {

    @Test
    public void nameShorterOrEqualTo40ReturnIt() {
        String name = "1234567890123456789012345678901234567890";
        String fileName = new FileNameGenerator().generate(name);
        assertThat(fileName).isEqualToIgnoringCase(name);
    }

    @Test
    public void nameLongerThan40ShortenItTo40() {
        String name= "12345678901234567890123456789012345678901";
        String fileName = new FileNameGenerator().generate(name);
        assertThat(fileName.length()).isLessThanOrEqualTo(40);
    }

    @Test
    public void wontGenerateSameNameForTwoDifferentNames() {
        String nameOne = "98765432109876543210987654321098765432109876543210";
        String nameTwo = "12345678901234567890123456789012345678901234567890";
        assertThat(new FileNameGenerator().generate(nameOne)).isNotEqualToIgnoringCase(new FileNameGenerator().generate(nameTwo));
    }

    @Test
    public void willReplaceIllegalCharsWithUnderscoreAndAddHashAsHex() {
        String name = "Test\tif/name/is#fixed or not";
        String fileName = new FileNameGenerator().generate(name);
        assertThat(fileName).isEqualToIgnoringCase("Test_if_name_is_fixed_or_not_f9e4eeb2");
    }

    @Test
    public void wontGenerateSameForSimilar() {
        String nameOne = "Test\tif/name/is#fixed or not";
        String nameTwo = "Test\tif\tname/is#fixed or not";
        assertThat(new FileNameGenerator().generate(nameOne)).isNotEqualToIgnoringCase(new FileNameGenerator().generate(nameTwo));
    }

    @Test
    public void withJapanese() {
        String name = "こんにちは";
        String fileName = new FileNameGenerator().generate(name);
        assertThat(fileName).isEqualToIgnoringCase("______bfca3f39");
    }
}