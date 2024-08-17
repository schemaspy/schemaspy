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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.beust.jcommander.JCommander;
import org.dummy.DummyDriverUnsatisfiedCtor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;
import org.schemaspy.testing.H2MemoryExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Nils Petzaell
 */
class DbDriverLoaderTest {

  @RegisterExtension
  static H2MemoryExtension h2 = new H2MemoryExtension("DbDriverLoaderTest");

  @Test
  void testGetConnection() {
    assertThat(
        new DriverFromConfig(
            parse("-t", Paths.get("src", "test", "resources", "integrationTesting", "dbTypes", "h2memory.properties").toString(), "-u", "sa", "-db", "DbDriverLoaderTest")
        ).driver()
    ).isNotNull();
  }

  @Test
  void driverLoaderCachesDrivers() {
    String[] drivers = new String[]{"org.h2.Driver"};

    DbDriverLoader driverLoader1 = new DbDriverLoader(drivers, Collections::emptyIterator);
    Driver driver1 = driverLoader1.driver();
    Driver driver2 = driverLoader1.driver();
    assertThat(driver1).isSameAs(driver2);
  }

  @Test
  void driverPathWorks() throws SQLException {
    Path driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar");
    DbDriverLoader driverLoader = new DbDriverLoader(
      new String[]{"dummy.DummyDriver"},
      () -> List.of(driverPath).iterator());
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.acceptsURL("dummy")).isTrue();
  }

  @Test
  void nativeErrorInDriverCreationPassesUncaught() {
    String[] drivers = new String[]{DummyDriverUnsatisfiedCtor.class.getName(), "dummy.dummy"};
    DbDriverLoader driverLoader = new DbDriverLoader(drivers, Collections::emptyIterator);
    assertThatExceptionOfType(UnsatisfiedLinkError.class)
        .isThrownBy(driverLoader::driver);
  }

  @Test
  void firstDriverClassMissingSecondExists() {
    DbDriverLoader driverLoader = new DbDriverLoader(
      new String[]{"com.no", "org.h2.Driver"},
      Collections::emptyIterator
    );
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("org.h2.Driver");
  }

  @Test
  void twoDriversBothExists() {
    DbDriverLoader driverLoader = new DbDriverLoader(
      new String[]{"com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver"},
      Collections::emptyIterator
    );
    Driver driver = driverLoader.driver();
    assertThat(driver).isNotNull();
    assertThat(driver.getClass().getName()).isEqualTo("com.mysql.cj.jdbc.Driver");
  }

  private ConnectionConfig parse(String... args) {
    DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli(new SimplePropertiesResolver());
    ConnectionConfigCli connectionConfigCli = new ConnectionConfigCli(databaseTypeConfigCli);
    JCommander jCommander = JCommander.newBuilder().build();
    jCommander.addObject(databaseTypeConfigCli);
    jCommander.addObject(connectionConfigCli);
    jCommander.parse(args);
    return connectionConfigCli;
  }
}
