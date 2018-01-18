package org.schemaspy.input.db.driver;

import org.h2.engine.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.schemaspy.input.db.driver.Loader;
import org.schemaspy.input.db.driver.LoaderConfigFluent;
import org.schemaspy.input.db.driver.LoaderException;

import java.sql.Driver;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

public class LoaderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void canLoadDriverFromDriverPath() throws SQLException, LoaderException {
        LoaderConfigFluent config = new LoaderConfigFluent()
                .driverClass("dummy.DummyDriver")
                .driverPath("src/test/resources/driverFolder/dummy.jar");
        Loader loader = new Loader(config);
        Driver driver = loader.getDriver();
        assertThat(driver.acceptsURL("dummy")).isTrue();
    }

    @Test
    public void canNotLoadDriverFromDriverPathInstantiationException() throws LoaderException {
        thrown.expectCause(instanceOf(InstantiationException.class));
        LoaderConfigFluent config = new LoaderConfigFluent()
                .driverClass("dummy.DummyDriverAbstract")
                .driverPath("src/test/resources/driverFolder/dummy.jar");
        Loader loader = new Loader(config);
        Driver driver = loader.getDriver();
        assertThat(driver).isNull();
    }

    @Test
    public void canNotLoadDriverFromDriverPathIllegalAccess() throws LoaderException {
        thrown.expectCause(instanceOf(IllegalAccessException.class));
        LoaderConfigFluent config = new LoaderConfigFluent()
                .driverClass("dummy.DummyDriverPrivateCtor")
                .driverPath("src/test/resources/driverFolder/dummy.jar");
        Loader loader = new Loader(config);
        Driver driver = loader.getDriver();
        assertThat(driver).isNull();
    }

    @Test
    public void canNotLoadDriverFromDriverPath() throws LoaderException {
        thrown.expectCause(instanceOf(ClassNotFoundException.class));
        LoaderConfigFluent config = new LoaderConfigFluent()
                .driverClass("dummy.DummyDriver")
                .driverPath("src/test/resources/driverFolder/dummy.nar");
        Loader loader = new Loader(config);
        Driver driver = loader.getDriver();
        assertThat(driver).isNull();
    }

    @Test
    public void canLoadDriverFromSystemClassLoader() throws LoaderException, SQLException {
        LoaderConfigFluent config = new LoaderConfigFluent()
                .driverClass(org.h2.Driver.class.getName());
        Loader loader = new Loader(config);
        Driver driver = loader.getDriver();
        assertThat(driver.acceptsURL(Constants.START_URL)).isTrue();
    }

}
