/*
 * Copyright (C) 2017 Mårten Bohlin
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
package org.schemaspy.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Samuel Dussault
 */
public class TypeTest {

    @Test
    public void definition() {
        // Given
        String expectedDefinition = "integer NOT NULL\n" +
                "DEFAULT 1\n" +
                "CHECK (VALUE < 4)";
        Type type = new Type(
                "Domain",
                null,
                "type_tests",
                "test_domain",
                "Description for domain type_tests.test_domain",
                expectedDefinition);

        // When
        String actualDefinition = type.getDefinition();

        // Then
        assertThat(actualDefinition).isEqualTo(expectedDefinition);
    }

    @Test
    public void typeCompareToUsingTypeofTypeCatalogSchemaAndNameIgnoreCase() {
        // Given
        Type type1 = new Type(
                "Domain",
                null,
                "type_tests",
                "test_domain",
                "Description for domain type_tests.test_domain",
                "integer NOT NULL");
        Type type2 = new Type(
                "DOMAIN",
                null,
                "TYPE_TESTS",
                "TEST_DOMAIN",
                "Any other description",
                "Any other definition");

        // When
        int areEquals = type1.compareTo(type2);

        // Then
        assertThat(areEquals).isEqualTo(0);
    }

}