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

import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.driverpath.Driverpath;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GetExistingUrlsTest {

  private final Path driverFolder = Paths.get("src", "test", "resources", "driverFolder");

  @Test
  void willAddDirAndContentIfDpIsADirAndNotAFile() {
    URI dummyJarURI = driverFolder.resolve("dummy.jar").toUri();
    URI dummyNarURI = driverFolder.resolve("dummy.nar").toUri();
    URI narJarWarNotIncludedURI = driverFolder.resolve("nar.jar.war.not.included").toUri();

    Driverpath dp = driverFolder::toString;
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder.toUri(), dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }

  @Test
  void willOnlyAddFileIfFileIsSpecified() {
    URI dummyJarURI = driverFolder.resolve("dummy.jar").toUri();

    Driverpath dp = () -> driverFolder.resolve("dummy.jar").toString();
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(1)
        .contains(dummyJarURI);
  }

  @Test
  void willAddDirAndContentIfDpSecondArgIsADirAndNotAFile() {
    URI dummyJarURI = driverFolder.resolve("dummy.jar").toUri();
    URI dummyNarURI = driverFolder.resolve("dummy.nar").toUri();
    URI narJarWarNotIncludedURI = driverFolder.resolve("nar.jar.war.not.included").toUri();

    String dpFile = driverFolder.resolve("dummy.jar").toString();
    String dpDir = driverFolder.toString();
    Set<URI> uris = new GetExistingUrls(() -> dpFile + File.pathSeparator + dpDir).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder.toUri(), dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }
}
