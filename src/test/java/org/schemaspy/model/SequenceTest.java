package org.schemaspy.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Sequence}.
 */
class SequenceTest {

    /**
     * Given two sequences with different names,
     * When one of the sequences is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareNames() {
        assertThat(
            new Sequence("foo", 0, 0).compareTo(new Sequence("bar", 0, 0))
        ).isNotEqualTo(0);
    }

    /**
     * Given two sequences with different start values,
     * When one of the sequences is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareStartValues() {
        assertThat(
            new Sequence("", 1, 0).compareTo(new Sequence("", 2, 0))
        ).isNotEqualTo(0);
    }

    /**
     * Given two sequences with different increments,
     * When one of the sequences is asked to compare itself to the other,
     * Then it should respond that they differ.
     */
    @Test
    void compareIncrements() {
        assertThat(
            new Sequence("", 0, 1).compareTo(new Sequence("", 0, 2))
        ).isNotEqualTo(0);
    }

    /**
     * Given two sequences with similar properties,
     * When one of the sequences is asked to compare itself to the other,
     * Then it should respond that they match.
     */
    @Test
    void compareEquality() {
        assertThat(
            new Sequence("", 0, 0).compareTo(new Sequence("", 0, 0))
        ).isEqualTo(0);
    }
}
