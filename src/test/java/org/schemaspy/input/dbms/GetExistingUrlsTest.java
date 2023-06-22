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

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GetExistingUrlsTest {

  @Test
  public void willAddDirAndContentIfDpIsADirAndNotAFile() {
    URI driverFolder = Paths.get("src", "test", "resources", "driverFolder").toUri();
    URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();
    URI dummyNarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.nar").toUri();
    URI narJarWarNotIncludedURI = Paths.get("src", "test", "resources", "driverFolder", "nar.jar.war.not.included")
                                       .toUri();

    String dp = Paths.get("src", "test", "resources", "driverFolder").toString();
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }

  @Test
  public void willOnlyAddFileIfFileIsSpecified() {
    URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();

    String dp = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
    Set<URI> uris = new GetExistingUrls(dp).paths();

    assertThat(uris)
        .hasSize(1)
        .contains(dummyJarURI);
  }

  @Test
  public void willAddDirAndContentIfDpSecondArgIsADirAndNotAFile() {
    URI driverFolder = Paths.get("src", "test", "resources", "driverFolder").toUri();
    URI dummyJarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toUri();
    URI dummyNarURI = Paths.get("src", "test", "resources", "driverFolder", "dummy.nar").toUri();
    URI narJarWarNotIncludedURI = Paths.get("src", "test", "resources", "driverFolder", "nar.jar.war.not.included")
                                       .toUri();

    String dpFile = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar").toString();
    String dpDir = Paths.get("src", "test", "resources", "driverFolder").toString();
    Set<URI> uris = new GetExistingUrls(dpFile + File.pathSeparator + dpDir).paths();

    assertThat(uris)
        .hasSize(4)
        .contains(driverFolder, dummyJarURI, dummyNarURI, narJarWarNotIncludedURI);
  }
}