package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DurationFormatterTest {

    @Test
    public void lessThenOneSecond() {
        String formatted = DurationFormatter.formatMS(899);
        assertThat(formatted).isEqualTo("899 ms");
    }

    @Test
    public void lessThenOneMinute() {
        String formatted = DurationFormatter.formatMS(12345);
        assertThat(formatted).isEqualTo("12 s 345 ms");
    }

    @Test
    public void lessThenOneHour() {
        String formatted = DurationFormatter.formatMS(123456);
        assertThat(formatted).isEqualTo("2 min 3 s 456 ms");
    }

    @Test
    public void moreThanOneHour() {
        String formatted = DurationFormatter.formatMS(12345678);
        assertThat(formatted).isEqualTo("3 hr 25 min 45 s 678 ms");
    }

    @Test
    public void exactlyOneMinute() {
        String formatted = DurationFormatter.formatMS(60000);
        assertThat(formatted).isEqualTo("1 min");
    }

}