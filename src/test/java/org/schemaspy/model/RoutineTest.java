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
 * @author Mårten Bohlin
 */
public class RoutineTest {

    @Test
    public void definition() throws Exception {
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
    public void nullDefinitionIsReplacedWithEmptyString() throws Exception {
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

}