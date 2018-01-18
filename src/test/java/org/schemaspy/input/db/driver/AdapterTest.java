package org.schemaspy.input.db.driver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.schemaspy.input.db.driver.*;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdapterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldUseConfiguration() throws LoaderException, SQLException {
        Loader loader = mock(Loader.class);
        Driver driver = mock(Driver.class);
        Connection connection = mock(Connection.class);
        when(loader.getDriver()).thenReturn(driver);

        ArgumentCaptor<String> connectionUrl = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Properties> info = ArgumentCaptor.forClass(Properties.class);

        when(driver.connect(connectionUrl.capture(), info.capture())).thenReturn(connection);
        AdapterConfigFluent config = new AdapterConfigFluent()
                .user("test")
                .password("test123")
                .connectionUrl("jdbc:testing:url");

        Adapter adapter = new Adapter(config, loader);
        Connection conn = adapter.getConnection();
        assertThat(conn).isNotNull();
        assertThat(connectionUrl.getValue()).isEqualToIgnoringCase("jdbc:testing:url");
        assertThat(info.getValue().getProperty("user")).isEqualTo("test");
        assertThat(info.getValue().getProperty("password")).isEqualTo("test123");
    }

    @Test
    public void wrapExceptionWhenLoaderFail() throws SQLException, LoaderException {
        thrown.expect(ConnectionException.class);
        thrown.expectCause(instanceOf(LoaderException.class));
        Loader loader = mock(Loader.class);
        when(loader.getDriver()).thenThrow(new LoaderException("Wrapperd", new Exception()));

        Adapter adapter = new Adapter(new AdapterConfigFluent(), loader);
        Connection conn = adapter.getConnection();
        assertThat(conn).isNull();
    }

    @Test
    public void createExceptionWhenConnectionIsNull() throws SQLException, LoaderException {
        thrown.expect(ConnectionException.class);
        thrown.expectCause(instanceOf(NullPointerException.class));
        Loader loader = mock(Loader.class);
        Driver driver = mock(Driver.class);
        when(loader.getDriver()).thenReturn(driver);
        when(driver.connect(anyString(), any())).thenReturn(null);

        Adapter adapter = new Adapter(new AdapterConfigFluent(), loader);
        Connection conn = adapter.getConnection();
        assertThat(conn).isNull();
    }
}
