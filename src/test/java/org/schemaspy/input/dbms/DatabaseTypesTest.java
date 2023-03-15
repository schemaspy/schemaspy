package org.schemaspy.input.dbms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseTypesTest {

    @Test
    void getBuildIn() {
        assertThat(
            new DatabaseTypes()
                .getBuiltInDatabaseTypes()
        )
            .contains("mysql");
    }
}