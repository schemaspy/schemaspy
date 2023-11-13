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

import com.beust.jcommander.JCommander;
import org.dummy.DummyDriver;
import org.dummy.DummyDriverUnsatisfiedConnect;
import org.dummy.DummyDriverUnsatisfiedCtor;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;
import org.schemaspy.input.dbms.driverpath.DpMissingPathChecked;
import org.schemaspy.input.dbms.exceptions.ConnectionFailure;
import org.schemaspy.testing.H2MemoryRule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
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
    assertThat(
        new DbDriverLoader(
            parse("-t", Paths.get("src", "test", "resources", "integrationTesting", "dbTypes", "h2memory.properties").toString(), "-u", "sa", "-db", "DbDriverLoaderTest")
        ).driver()
    ).isNotNull();
  }

  @Test
  public void driverLoaderCachesDrivers() {
    ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
    Mockito.when(builder.build()).thenReturn("");
    String[] drivers = new String[]{"org.h2.Driver"};

    DbDriverLoader driverLoader1 = new DbDriverLoader(parse(), builder, drivers, () -> "");
    Driver driver1 = driverLoader1.driver();
    DbDriverLoader driverLoader2 = new DbDriverLoader(parse(), builder, drivers, () -> "");
    Driver driver2 = driverLoader2.driver();
    assertThat(driver1).isSameAs(driver2);
  }

  @Test
  public void driverPathWorks() throws SQLException {
    String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
    ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
    Mockito.when(builder.build()).thenReturn("");
    DbDriverLoader driverLoader = new DbDriverLoader(parse(), builder, new String[]{"dummy.DummyDriver"}, () -> driverPath);
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.acceptsURL("dummy")).isTrue();
  }

  @Test
  public void nativeErrorInDriverCreationPassesUncaught() {
    ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
    Mockito.when(builder.build()).thenReturn("dummy");
    String[] drivers = new String[]{DummyDriverUnsatisfiedCtor.class.getName(), "dummy.dummy"};
    DbDriverLoader driverLoader = new DbDriverLoader(parse(), builder, drivers, () -> "");
    assertThatExceptionOfType(UnsatisfiedLinkError.class)
        .isThrownBy(driverLoader::driver);
  }

  @Test
  public void firstDriverClassMissingSecondExists() {
    ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
    Mockito.when(builder.build()).thenReturn("");
    DbDriverLoader driverLoader = new DbDriverLoader(parse(), builder, new String[]{"com.no", "org.h2.Driver"}, () -> "");
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("org.h2.Driver");
  }

  @Test
  public void twoDriversBothExists() {
    ConnectionURLBuilder builder = Mockito.mock(ConnectionURLBuilder.class);
    Mockito.when(builder.build()).thenReturn("");
    DbDriverLoader driverLoader = new DbDriverLoader(parse(), builder, new String[]{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"}, () -> "");
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("com.mysql.cj.jdbc.Driver");
  }

  ConnectionConfig parse(String... args) {
    DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli(new SimplePropertiesResolver());
    ConnectionConfigCli connectionConfigCli = new ConnectionConfigCli(databaseTypeConfigCli);
    JCommander jCommander = JCommander.newBuilder().build();
    jCommander.addObject(databaseTypeConfigCli);
    jCommander.addObject(connectionConfigCli);
    jCommander.parse(args);
    return connectionConfigCli;
  }
}
