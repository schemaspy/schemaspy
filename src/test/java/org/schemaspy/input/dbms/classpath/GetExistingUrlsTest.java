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
package org.schemaspy.input.dbms.classpath;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.driverpath.Driverpath;

import static org.assertj.core.api.Assertions.assertThat;

class GetExistingUrlsTest {

  private final Path driverFolder = Paths.get("src", "test", "resources", "driverFolder");

  @Test
  void willAddDirAndContentIfDpIsADirAndNotAFile() {
    Path dummyJar = driverFolder.resolve("dummy.jar");
    Path dummyNar = driverFolder.resolve("dummy.nar");
    Path narJarWarNotIncluded = driverFolder.resolve("nar.jar.war.not.included");

    Driverpath dp = () -> List.of(driverFolder).iterator();
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder.toUri(), dummyJar.toUri(), dummyNar.toUri(), narJarWarNotIncluded.toUri());
  }

  @Test
  void willOnlyAddFileIfFileIsSpecified() {
    Path dummyJar = driverFolder.resolve("dummy.jar");

    Driverpath dp = () -> List.of(dummyJar).iterator();
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(1)
        .contains(dummyJar.toUri());
  }

  @Test
  void willAddDirAndContentIfDpSecondArgIsADirAndNotAFile() {
    Path dummyJar = driverFolder.resolve("dummy.jar");
    Path dummyNar = driverFolder.resolve("dummy.nar");
    Path narJarWarNotIncluded = driverFolder.resolve("nar.jar.war.not.included");

    Path dpFile = driverFolder.resolve("dummy.jar");
    Path dpDir = driverFolder;
    Set<URI> uris = new GetExistingUrls(() -> List.of(dpFile, dpDir).iterator()).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder.toUri(), dummyJar.toUri(), dummyNar.toUri(), narJarWarNotIncluded.toUri());
  }
}
