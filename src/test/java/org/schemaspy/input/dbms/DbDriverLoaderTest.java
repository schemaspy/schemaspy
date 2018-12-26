/*
 * Copyright (C) 2017, 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms;

import org.dummy.DummyDriver;
import org.dummy.DummyDriverUnsatisfiedConnect;
import org.dummy.DummyDriverUnsatisfiedCtor;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
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

/**
 * @author Nils Petzaell
 */
public class DbDriverLoaderTest {

  @Rule
  public H2MemoryRule h2 = new H2MemoryRule("DbDriverLoaderTest");

  @Test
  public void testGetConnection() throws IOException {
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    Config config = new Config("-t", "h2", "-u", "sa");
    Connection connection = dbDriverLoader.getConnection(config, h2.getConnectionURL(), toArray("org.h2.Driver"), "");
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
    Driver driver1 = driverLoader1.getDriver(toArray("org.h2.Driver"),"");
    DbDriverLoader driverLoader2 = new DbDriverLoader();
    Driver driver2 = driverLoader2.getDriver(toArray("org.h2.Driver"),"");
    assertThat(driver1).isSameAs(driver2);
  }

  @Test
  public void driverPathWorks() throws MalformedURLException, SQLException {
    String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
    DbDriverLoader driverLoader = new DbDriverLoader();
    Driver driver = driverLoader.getDriver(toArray("dummy.DummyDriver"), driverPath);
    assertThat(driver).isNotNull();
    assertThat(driver.acceptsURL("dummy")).isTrue();
  }

  @Test
  public void connectionIsNullThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", toArray(DummyDriver.class.getName()), ""));
  }

  @Test
  public void nativeErrorInDriverCreationThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", toArray(DummyDriverUnsatisfiedCtor.class.getName(), "dummy.dummy"), ""))
            .withCauseInstanceOf(UnsatisfiedLinkError.class)
            .withMessageContaining("Error with native library occurred while trying to use driver 'org.dummy.DummyDriverUnsatisfiedCtor,dummy.dummy'");
  }

  @Test
  public void nativeErrorInConnectThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", toArray(DummyDriverUnsatisfiedConnect.class.getName()), ""))
            .withCauseInstanceOf(UnsatisfiedLinkError.class)
            .withMessageContaining("Error with native library occurred while trying to use driver 'org.dummy.DummyDriverUnsatisfiedConnect'");
  }

  @Test
  public void DriverMissingWithClasspathThrowsException() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    String sep = File.separator;
    final String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString() + File.pathSeparator + "missing";
    assertThatExceptionOfType(ConnectionFailure.class)
            .isThrownBy(() -> driverLoader.getConnection(new Config("-sso", "-o", "someplace"), "dummy", toArray("bla.bla.bla", "no.no.no"), driverPath))
            .withCauseInstanceOf(ConnectionFailure.class)
            .withMessageContaining("'bla.bla.bla, no.no.no'")
            .withMessageContaining("src" + sep + "test" + sep + "resources" + sep + "driverFolder" + sep + "dummy.jar"+File.pathSeparator+"missing")
            .withMessageContaining("There were missing paths in driverPath:"+System.lineSeparator()+"\tmissing");
  }

  private String[] toArray(String...strings) {
    return strings;
  }

  @Test
  public void firstDriverClassMissingSecondExists() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    Driver driver = driverLoader.getDriver(toArray("com.no","org.h2.Driver"),"");
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("org.h2.Driver");
  }

  @Test
  public void twoDriversBothExists() {
    DbDriverLoader driverLoader = new DbDriverLoader();
    Driver driver = driverLoader.getDriver(toArray("com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"),"");
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("com.mysql.cj.jdbc.Driver");
  }

  @Test
  public void willAddDirAndContentIfDpIsADirAndNotAFile() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    URI driverFolder = Paths.get("src","test", "resources", "driverFolder").toUri();
    URI dummyJarURI = Paths.get("src","test", "resources", "driverFolder","dummy.jar").toUri();
    URI dummyNarURI = Paths.get("src","test", "resources", "driverFolder","dummy.nar").toUri();
    URI narJarWarNotIncludedURI = Paths.get("src","test", "resources", "driverFolder","nar.jar.war.not.included").toUri();
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    String dp = Paths.get("src","test", "resources", "driverFolder").toString();

    Method method = DbDriverLoader.class.getDeclaredMethod("getExistingUrls", String.class);
    method.setAccessible(true);

    Set<URI> uris = (Set<URI>) method.invoke(dbDriverLoader, dp);
    assertThat(uris.size()).isEqualTo(4);
    assertThat(uris).contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }

  @Test
  public void willOnlyAddFileIfFileIsSpecified() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    URI dummyJarURI = Paths.get("src","test", "resources", "driverFolder","dummy.jar").toUri();
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    String dp = Paths.get("src","test", "resources", "driverFolder", "dummy.jar").toString();

    Method method = DbDriverLoader.class.getDeclaredMethod("getExistingUrls", String.class);
    method.setAccessible(true);

    Set<URI> uris = (Set<URI>) method.invoke(dbDriverLoader, dp);
    assertThat(uris.size()).isEqualTo(1);
    assertThat(uris).contains(dummyJarURI);
  }

  @Test
  public void willAddDirAndContentIfDpSecondArgIsADirAndNotAFile() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    URI driverFolder = Paths.get("src","test", "resources", "driverFolder").toUri();
    URI dummyJarURI = Paths.get("src","test", "resources", "driverFolder","dummy.jar").toUri();
    URI dummyNarURI = Paths.get("src","test", "resources", "driverFolder","dummy.nar").toUri();
    URI narJarWarNotIncludedURI = Paths.get("src","test", "resources", "driverFolder","nar.jar.war.not.included").toUri();
    DbDriverLoader dbDriverLoader = new DbDriverLoader();
    String dpFile = Paths.get("src","test", "resources", "driverFolder", "dummy.jar").toString();
    String dpDir = Paths.get("src","test", "resources", "driverFolder").toString();

    Method method = DbDriverLoader.class.getDeclaredMethod("getExistingUrls", String.class);
    method.setAccessible(true);

    Set<URI> uris = (Set<URI>) method.invoke(dbDriverLoader, dpFile + File.pathSeparator + dpDir);
    assertThat(uris.size()).isEqualTo(4);
    assertThat(uris).contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }
}
