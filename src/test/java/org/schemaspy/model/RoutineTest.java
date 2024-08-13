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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mårten Bohlin
 */
class RoutineTest {

    @Test
    void definition() {
        // Given
        String procedureDefinition = "create procedure dbo.TestProcedure (@param varchar(max) AS\nselect * from dbo.TestTable";
        Routine routine = new Routine(
                "TestProcedure",
                "PROCEDURE",
                null,
                "SQL",
                procedureDefinition,
                false,
                "MODIFIES",
                null,
                "Comment");

        // When
        String definition = routine.getDefinition();

        // Then
        assertThat(definition).isEqualTo(procedureDefinition);
    }

    @Test
    void nullDefinitionIsReplacedWithEmptyString() {
        // Given
        Routine routine = new Routine(
                "testFunction",
                "FUNCTION",
                "varchar(10)",
                "EXTERNAL",
                null,
                false,
                "READS",
                null,
                "Comment");

        // When
        String definition = routine.getDefinition();

        // Then
        assertThat(definition).isEqualTo("");
    }

    /**
     * Given two routines with different names,
     * When one of the routines is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareNames() {
        assertThat(
            new Routine("foo", "", "", "", "", false, "", "", "").compareTo(
                new Routine("bar", "", "", "", "", false, "", "", "")
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two routines with different types,
     * When one of the routines is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareTypes() {
        assertThat(
            new Routine("", "foo", "", "", "", false, "", "", "").compareTo(
                new Routine("", "bar", "", "", "", false, "", "", "")
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two routines with different return types,
     * When one of the routines is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareReturnTypes() {
        assertThat(
            new Routine("", "", "foo", "", "", false, "", "", "").compareTo(
                new Routine("", "", "bar", "", "", false, "", "", "")
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two routines with different definitions,
     * When one of the routines is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareDefinitions() {
        assertThat(
            new Routine("", "", "", "", "foo", false, "", "", "").compareTo(
                new Routine("", "", "", "", "bar", false, "", "", "")
            )
        ).isNotEqualTo(0);
    }

    /**
     * Given two routines with similar properties,
     * When one of the routines is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareEquality() {
        assertThat(
            new Routine("", "", "", "", "", false, "", "", "").compareTo(
                new Routine("", "", "", "", "", false, "", "", "")
            )
        ).isEqualTo(0);
    }
}
