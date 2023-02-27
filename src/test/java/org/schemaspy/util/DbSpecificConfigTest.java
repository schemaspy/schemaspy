/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.util;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class DbSpecificConfigTest {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    private static Properties withoutHostAndOptionalPort = new Properties();
    private static Properties withHostAndOptionalPort = new Properties();

    @BeforeClass
    public static void setupProperties() {
        withoutHostAndOptionalPort.setProperty("description","MySQL");
        withoutHostAndOptionalPort.setProperty("connectionSpec","jdbc:mysql://<host>/<db>?socketFactory=<socketFactory>&socket=<socket>");
        withoutHostAndOptionalPort.setProperty("host","host where database resides with optional port");
        withoutHostAndOptionalPort.setProperty("port","port database is listening on");
        withoutHostAndOptionalPort.setProperty("db","database name");
        withoutHostAndOptionalPort.setProperty("socketFactory","ClassName of socket factory which must be in your classpath");
        withoutHostAndOptionalPort.setProperty("socket","Path To Socket");

        withHostAndOptionalPort.setProperty("description","MySQL");
        withHostAndOptionalPort.setProperty("connectionSpec","jdbc:mysql://<hostOptionalPort>/<db>");
        withHostAndOptionalPort.setProperty("host","host where database resides with optional port");
        withHostAndOptionalPort.setProperty("port","port database is listening on");
        withHostAndOptionalPort.setProperty("db","database name");
    }

    @Test
    public void worksWithoutHostAndOptionalPort() {
        DbSpecificConfig dbSpecificConfig = new DbSpecificConfig("withoutHostAndOptionalPort", withoutHostAndOptionalPort);
        assertThat(dbSpecificConfig.getOptions()).usingElementComparatorOnFields("name", "description").containsExactly(
                new DbSpecificOption("host", "host where database resides with optional port"),
                new DbSpecificOption("db", "database name"),
                new DbSpecificOption("socketFactory", "ClassName of socket factory which must be in your classpath"),
                new DbSpecificOption("socket", "Path To Socket")
        );
    }

    @Test
    @Logger(DbSpecificConfig.class)
    public void dumpUsageWithoutHostAndOptionalPort() {
        DbSpecificConfig dbSpecificConfig = new DbSpecificConfig("withoutHostAndOptionalPort", withoutHostAndOptionalPort);
        dbSpecificConfig.dumpUsage();
        assertThat(loggingRule.getLog()).isEqualTo("   MySQL (-t withoutHostAndOptionalPort)      -host   \t\thost where database resides with optional port      -db   \t\tdatabase name      -socketFactory   \t\tClassName of socket factory which must be in your classpath      -socket   \t\tPath To Socket");
    }

    @Test
    public void worksWithHostAndOptionalPort() {
        DbSpecificConfig dbSpecificConfig = new DbSpecificConfig("withHostAndOptionalPort", withHostAndOptionalPort);
        assertThat(dbSpecificConfig.getOptions()).usingElementComparatorOnFields("name", "description").containsExactly(
                new DbSpecificOption("hostOptionalPort", null),
                new DbSpecificOption("db", "database name")
        );
    }

    @Test
    @Logger(DbSpecificConfig.class)
    public void dumpUsageWithHostAndOptionalPort(){
        DbSpecificConfig dbSpecificConfig = new DbSpecificConfig("withHostAndOptionalPort", withHostAndOptionalPort);
        dbSpecificConfig.dumpUsage();
        assertThat(loggingRule.getLog()).isEqualTo("   MySQL (-t withHostAndOptionalPort)      -host   \t\thost of database, may contain port      -port   \t\toptional port if not default      -db   \t\tdatabase name");
    }

}
