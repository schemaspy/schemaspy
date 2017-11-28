package org.schemaspy.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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