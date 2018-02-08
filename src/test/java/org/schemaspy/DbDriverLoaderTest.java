package org.schemaspy;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.H2MemoryRule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    Set<URL> urls = new HashSet<>();
    String driverPath = "src/test/resources/driverFolder/dummy.jar";
    Method loadAdditionalJarsFromDriver = DbDriverLoader.class.getDeclaredMethod("loadAdditionalJarsForDriver", String.class, Set.class);
    loadAdditionalJarsFromDriver.setAccessible(true);
    loadAdditionalJarsFromDriver.invoke(dbDriverLoader, driverPath, urls);
    assertThat(urls).contains(Paths.get(driverPath).toUri().toURL());
    assertThat(urls).contains(Paths.get(driverPath).resolveSibling("dummy.nar").toUri().toURL());
    assertThat(urls).doesNotContain(Paths.get(driverPath).resolveSibling("nar.jar.war.not.included").toUri().toURL());
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
}
