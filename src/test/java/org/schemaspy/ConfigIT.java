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
package org.schemaspy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;
import org.schemaspy.util.DbSpecificConfig;

/**
 * @author Nils Petzaell
 */
public class ConfigIT {
	@Rule
	public LoggingRule loggingRule = new LoggingRule();

	@Test
	@Logger(DbSpecificConfig.class)
	public void onlyOutputSelectedDatabaseTypeWhenDbSpecific() {
		Config config = new Config("-t", "mysql");
		config.dumpUsage("Test", true);
		assertThat(loggingRule.getLog()).contains("MySQL");
		assertThat(loggingRule.getLog()).doesNotContain("Microsoft SQL Server");
	}

}
