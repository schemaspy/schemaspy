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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LoadAdditionalJarsForDriverTest {

  @Test
  public void testLoadAdditionalJarsForDriver() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    LoadAdditionalJarsForDriver loadAdditionalJarsForDriver = new LoadAdditionalJarsForDriver();
    Set<URI> urls = new HashSet<>();
    String driverPath = "src/test/resources/driverFolder/dummy.jar";
    Method loadAdditionalJarsFromDriver = LoadAdditionalJarsForDriver.class.getDeclaredMethod(
            "loadAdditionalJarsForDriver",
            String.class,
            Set.class
    );
    loadAdditionalJarsFromDriver.setAccessible(true);
    loadAdditionalJarsFromDriver.invoke(loadAdditionalJarsForDriver, driverPath, urls);
    assertThat(urls)
            .contains(Paths.get(driverPath).toUri())
            .contains(Paths.get(driverPath).resolveSibling("dummy.nar").toUri())
            .doesNotContain(Paths.get(driverPath).resolveSibling("nar.jar.war.not.included").toUri());
  }
}