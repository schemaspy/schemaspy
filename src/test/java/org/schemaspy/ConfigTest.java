/*
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Nils Petzaell
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
package org.schemaspy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Wojciech Kasa
 * @author Nils Petzaell
 */
public class ConfigTest {

    @Test
    public void testConfig() throws Exception {
        String[] args = {"-t", "mssql05", "-schemas", "dbo, sys", "-h"};

        Config config = new Config(args);
        assertThat(config.getSchemas()).hasSize(2);
        assertThat(config.getDbType()).isEqualToIgnoringCase("mssql05");
    }

    @Test
    public void testLoadJars() {
        Config config = new Config("-loadjars", "true");
        assertThat(config.isLoadJDBCJarsEnabled()).isTrue();
    }

    @Test
    public void testLoadProperties() {
        Config config = new Config("-configFile", "src/test/resources/configTest/loadpropertiesTest.properties");
        assertThat(config.getPassword()).isEqualToIgnoringCase("database_password");
        assertThat(config.getUser()).isEqualToIgnoringCase("database_user");
        assertThat(config.getPort()).isEqualTo(123);
    }

    @Test
    public void propertiesShouldHaveTrailingSpacesTrimmed() {
        Config config = new Config("-configFile", "src/test/resources/configTest/propertiesWithTrailingSpace.properties");
        assertThat(config.getPassword()).isEqualToIgnoringCase("database_password");
        assertThat(config.getUser()).isEqualToIgnoringCase("database_user");
        assertThat(config.getDb()).isEqualToIgnoringCase("db_name");
    }

    @Test
    public void propertiesShouldHaveTrailingSpaces() {
        Config config = new Config("-configFile", "src/test/resources/configTest/propertiesWithTrailingSpaceRetained.properties");
        assertThat(config.getPassword()).isEqualToIgnoringCase("database_password ");
        assertThat(config.getUser()).isEqualToIgnoringCase("database_user");
        assertThat(config.getDb()).isEqualToIgnoringCase("db_name");
    }

    @Test
    public void exportedKeysIsEnabledByDefault() {
        Config config = new Config();
        assertThat(config.isExportedKeysEnabled()).isTrue();
    }

    @Test
    public void exportedKeysCanBeDisabled() {
        Config config = new Config("-noexportedkeys");
        assertThat(config.isExportedKeysEnabled()).isFalse();
    }

}