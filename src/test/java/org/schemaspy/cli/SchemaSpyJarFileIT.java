package org.schemaspy.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaSpyJarFileIT {

    @Test
    void path() {
        assertThat(new SchemaSpyJarFile().path().toString())
            .matches(".*schemaspy-[\\d\\.]+(-SNAPSHOT)?\\.jar(\\.original)?$");
    }

}