package org.schemaspy.connection;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.schemaspy.input.dbms.ConnectionURLBuilder;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ScExceptionChecked}.
 */
class ScExceptionCheckedTest {

    /**
     * Given that the origin cannot connect,
     * When the object is asked for the connection,
     * Then it should throw a connection failure exception.
     */
    @Test
    void ThrowException() {
        final ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
        final ScExceptionChecked sut = new ScExceptionChecked(builder, () -> {
            throw new ConnectionFailure("");
        });
        assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(sut::connection)
            .withCauseInstanceOf(ConnectionFailure.class);
    }
}
