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
package org.schemaspy.input.dbms.config;

import org.junit.Test;
import org.schemaspy.input.dbms.exceptions.ResourceNotFoundException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class PropertiesFinderTest {

    private PropertiesFinder propertiesFinder = new PropertiesFinder();

    @Test
    public void findOnClassPathWithExtension() {
        URL url = propertiesFinder.find("A0.properties");
        assertThat(url).isNotNull();
    }
    @Test
    public void findOnClassPathWithNoExtension() {
        URL url = propertiesFinder.find("A0");
        assertThat(url).isNotNull();
    }

    @Test
    public void findByPathWithOutExtension() {
        URL url = propertiesFinder.find("src/test/resources/dbtypes/C0");
        assertThat(url).isNotNull();
    }

    @Test
    public void findByPathWithExtension() {
        URL url = propertiesFinder.find("src/test/resources/dbtypes/C0.properties");
        assertThat(url).isNotNull();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void noSuchResource() {
        propertiesFinder.find("doesNotExist");
    }

    @Test
    public void shouldNotReturnAFolderFromClassPath() throws URISyntaxException {
        URL url = propertiesFinder.find("folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }

    @Test
    public void shouldNotReturnAFolderFromPath() throws URISyntaxException {
        URL url = propertiesFinder.find("src/test/resources/org/schemaspy/types/folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }
}
