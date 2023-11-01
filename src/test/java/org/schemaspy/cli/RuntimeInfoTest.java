package org.schemaspy.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeInfoTest {

    @Test
    void expectedOutput() {
        assertThat(
                new RuntimeInfo(
                        "App",
                        "1.0.0"
                ).toString()
        )
                .contains("Running App 1.0.0")
                .contains("Java(")
                .contains("started by");
    }

}