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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.schemaspy.input.dbms.exceptions.ResourceNotFoundException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Nils Petzaell
 */
class PropertiesFinderTest {

    private final PropertiesFinder propertiesFinder = new PropertiesFinder();

    @ParameterizedTest(name = "{index} {0} is found")
    @CsvSource(
            {
                    "onClassPathWithExtension, mssql.properties",
                    "onClassPathWithNoExtension, mssql",
                    "byPathWithExtension, src/test/resources/dbtypes/C0.properties",
                    "byPathWithNoExtension, src/test/resources/dbtypes/C0"
            }
    )
    void find(String type, String argument) {
        URL url = propertiesFinder.find(argument);
        assertThat(url).isNotNull().as("Should have found %s %s", argument, type);
    }

    @Test
    void noSuchResource() {
        assertThatThrownBy(() -> propertiesFinder.find("doesNotExist"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldNotReturnAFolderFromClassPath() throws URISyntaxException {
        URL url = propertiesFinder.find("folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }

    @Test
    void shouldNotReturnAFolderFromPath() throws URISyntaxException {
        URL url = propertiesFinder.find("src/test/resources/org/schemaspy/types/folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }
}
