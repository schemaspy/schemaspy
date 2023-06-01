package org.schemaspy.input.dbms.driverpath;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Tests for {@link DpFallback}.
 */
public class DpFallbackTest {

    /**
     * Given primary that gives a response,
     * When the object is asked for a driverpath,
     * Then it should respond with the primary's response.
     */
    @Test
    public void delegate()  {
        final Driverpath primary = () -> "foo";
        final Driverpath secondary = () -> "bar";
        assertThat(
            new DpFallback(
                primary,
                secondary
            ).value()
        ).isEqualTo(primary.value());
    }

    /**
     * Given primary that doesn't give a response,
     * When the object is asked for a driverpath,
     * Then it should respond with the secondary's response.
     */
    @Test
    public void fallback()  {
        final Driverpath primary = () -> null;
        final Driverpath secondary = () -> "foo";
        assertThat(
            new DpFallback(
                primary,
                secondary
            ).value()
        ).isEqualTo(secondary.value());
    }
}
