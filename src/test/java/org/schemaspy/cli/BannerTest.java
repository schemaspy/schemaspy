package org.schemaspy.cli;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BannerTest {

    @Test
    void replaceOneInBanner() {
        assertThat(
                new Banner("/banner_test.txt", Map.of("one","two")).banner()
        ).contains("I want two to be two");
    }
}