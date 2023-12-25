package org.schemaspy.connection;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ScNullChecked}.
 */
class ScNullCheckedTest {

    /**
     * Given that the origin will produce null,
     * When the object is asked for the connection,
     * Then it should throw an exception.
     */
    @Test
    void ThrowExceptionOnNull() {
        final ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
        Mockito.when(builder.build()).thenReturn("dummy");
        final ScNullChecked sut = new ScNullChecked(builder, () -> null);
        assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(sut::connection);
    }
}
