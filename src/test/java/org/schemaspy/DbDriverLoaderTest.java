package org.schemaspy;

import org.dummy.DummyDriver;
import org.dummy.DummyDriverUnsatisfiedConnect;
import org.dummy.DummyDriverUnsatisfiedCtor;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.model.ConnectionFailure;
import org.schemaspy.testing.H2MemoryRule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DbDriverLoaderTest {

  @Rule
  public H2MemoryRule h2 = new H2MemoryRule("DbDriverLoaderTest");

  @Test
  public void testGetConnection() throws IOException {
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    Config config = new Config("-t", "h2", "-u", "sa");
    Connection connection = dbDriverLoader.getConnection(config, h2.getConnectionURL(), "org.h2.Driver", "");
    assertThat(connection).isNotNull();
  }

  @Test
  public void testLoadAdditionalJarsForDriver() throws NoSuchFieldException, IllegalAccessException, MalformedURLException, NoSuchMethodException, InvocationTargetException {
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    Set<URI> urls = new HashSet<>();
    String driverPath = "src/test/resources/driverFolder/dummy.jar";
    Method loadAdditionalJarsFromDriver = DbDriverLoader.class.getDeclaredMethod("loadAdditionalJarsForDriver", String.class, Set.class);
    loadAdditionalJarsFromDriver.setAccessible(true);
    loadAdditionalJarsFromDriver.invoke(dbDriverLoader, driverPath, urls);
    assertThat(urls).contains(Paths.get(driverPath).toUri());
    assertThat(urls).contains(Paths.get(driverPath).resolveSibling("dummy.nar").toUri());
    assertThat(urls).doesNotContain(Paths.get(driverPath).resolveSibling("nar.jar.war.not.included").toUri());
  }

  @Test
  public void driverLoaderCachesDrivers() throws MalformedURLException {
    DbDriverLoader driverLoader1 = new DbDriverLoader();
    Driver driver1 = driverLoader1.getDriver("org.h2.Driver","");
    DbDriverLoader driverLoader2 = new DbDriverLoader();
    Driver driver2 = driverLoader2.getDriver("org.h2.Driver","");
    assertThat(driver1).isSameAs(driver2);
  }

  @Test
  public void driverPathWorks() throws MalformedURLException, SQLException {
    String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
    DbDriverLoader driverLoader = new DbDriverLoader();
    Driver driver = driverLoader.getDriver("dummy.DummyDriver", driverPath);
    assertThat(driver).isNotNull();
    assertThat(driver.acceptsURL("dummy")).isTrue();
  }

  @Test
  public void connectionIsNullThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", DummyDriver.class.getName(), ""));
  }

  @Test
  public void nativeErrorInDriverCreationThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", DummyDriverUnsatisfiedCtor.class.getName(), ""))
            .withCauseInstanceOf(UnsatisfiedLinkError.class)
            .withMessageContaining("Error with native library occurred while trying to use driver 'org.dummy.DummyDriverUnsatisfiedCtor'");
  }

  @Test
  public void nativeErrorInConnectThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", DummyDriverUnsatisfiedConnect.class.getName(), ""))
            .withCauseInstanceOf(UnsatisfiedLinkError.class)
            .withMessageContaining("Error with native library occurred while trying to use driver 'org.dummy.DummyDriverUnsatisfiedConnect'");
  }

  @Test
  public void DriverMissingWithClasspathThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    String sep = File.separator;
    final String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString() + File.pathSeparator + "missing";
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", "bla.bla.bla", driverPath))
            .withCauseInstanceOf(ConnectionFailure.class)
            .withMessageContaining("src" + sep + "test" + sep + "resources" + sep + "driverFolder" + sep + "dummy.jar"+File.pathSeparator+"missing")
            .withMessageContaining("There were missing paths in driverPath:"+System.lineSeparator()+"\tmissing");
  }
}
