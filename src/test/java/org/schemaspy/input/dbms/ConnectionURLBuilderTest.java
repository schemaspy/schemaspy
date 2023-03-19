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

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.config.SimplePropertiesResolver;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
class ConnectionURLBuilderTest {

    @Test
    void shouldReplaceHostInConnectionSpec() {
        String[] args = {
            "-t", "src/test/resources/dbtypes/onlyHost",
            "-host", "abc.com",
            "-u", "aUser"
        };
        assertThat(
            new ConnectionURLBuilder(
                parse(args)
            )
                .build()
        )
            .isEqualToIgnoringCase("abc.com");
    }

    @Test
    void shouldReplaceHostAndOrPortWithOnlyHost() {
        String[] args = {
            "-t", "src/test/resources/dbtypes/hostOptionalPort",
            "-host", "abc.com",
            "-u", "aUser"
        };
        assertThat(
            new ConnectionURLBuilder(
                parse(args)
            )
                .build()
        )
            .isEqualToIgnoringCase("abc.com");
    }

    @Test
    void shouldReplaceHostAndOrPortWithHostAndPort() {
        String[] args = {
            "-t", "src/test/resources/dbtypes/hostOptionalPort",
            "-host", "abc.com",
            "-port", "1234",
            "-u", "aUser"
        };
        assertThat(
            new ConnectionURLBuilder(
                parse(args)
            )
                .build()
        )
            .isEqualToIgnoringCase("abc.com:1234");
    }

    @Test
    void shouldReplaceHostAndOrPortWithHostAndPortOnlyOnce() {
        String[] args = {
            "-t", "src/test/resources/dbtypes/hostOptionalPort",
            "-host", "abc.com:4321",
            "-port", "1234",
            "-u", "aUser"
        };
        assertThat(
            new ConnectionURLBuilder(
                parse(args)
            )
                .build()
        )
            .isEqualToIgnoringCase("abc.com:4321");
    }

    @Test
    void shouldReplaceHostAndOrPortWithCustomSeparator() {
        String[] args = {
            "-t", "src/test/resources/dbtypes/hostOptionalPortCustomSeparator",
            "-host", "abc.com",
            "-port", "1234",
            "-u", "aUser"
        };
        assertThat(
            new ConnectionURLBuilder(
                parse(args)
            )
                .build()
        )
            .isEqualToIgnoringCase("abc.com|1234");
    }

    ConnectionConfig parse(String...args) {
        DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli(new SimplePropertiesResolver());
        ConnectionConfigCli connectionConfigCli = new ConnectionConfigCli(databaseTypeConfigCli);
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(databaseTypeConfigCli);
        jCommander.addObject(connectionConfigCli);
        jCommander.parse(args);
        return connectionConfigCli;
    }
}