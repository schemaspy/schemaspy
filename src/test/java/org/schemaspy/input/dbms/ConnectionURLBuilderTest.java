/*
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
package org.schemaspy.input.dbms;

import org.junit.Test;
import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class ConnectionURLBuilderTest {

    @Test
    public void shouldReplaceHostInConnectionSpec() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/onlyHost",
                "-host", "abc.com",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithOnlyHost() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithHostAndPort() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com:1234");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithHostAndPortOnlyOnce() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPort",
                "-host", "abc.com:4321",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com:4321");
    }

    @Test
    public void shouldReplaceHostAndOrPortWithCustomSeparator() throws IOException {
        Config config = new Config(
                "-t", "src/test/resources/dbtypes/hostOptionalPortCustomSeparator",
                "-host", "abc.com",
                "-port", "1234",
                "-u", "aUser");
        Properties properties = config.determineDbProperties(config.getDbType());
        ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, properties);
        assertThat(connectionURLBuilder.build()).isEqualToIgnoringCase("abc.com|1234");
    }
}