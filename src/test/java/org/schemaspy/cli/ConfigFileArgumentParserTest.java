/*
 * Copyright (C) 2017 Thomas Traude
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
package org.schemaspy.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Traude
 */
public class ConfigFileArgumentParserTest {

    @Test
    public void givenConfigFileArgument_ExpectToParseItsValue() {
        assertThat(
                new ConfigFileArgumentParser("-configFile", "my.properties")
                        .configFile()
        ).contains("my.properties");
    }

    @Test
    public void givenNoConfigFileArgument_ExpectEmptyConfigFileValue() {
        assertThat(
                new ConfigFileArgumentParser()
                        .configFile()
        ).isEmpty();
    }
}