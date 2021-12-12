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
package org.schemaspy.integrationtesting.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schemaspy.Config;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.integrationtesting.TestServiceFixture;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.H2MemoryRule;

/**
 * @author Nils Petzaell
 */
public class H2PrimaryKeySeqnoIT {
	private final TestServiceFixture serviceFixture = new TestServiceFixture();

	@ClassRule
	public static H2MemoryRule h2MemoryRule = new H2MemoryRule("pkorder")
			.addSqlScript("src/test/resources/integrationTesting/h2/dbScripts/pkordering.sql");

	@Mock
	private ProgressListener progressListener;

	@Mock
	private CommandLineArguments arguments;

	private static Database database;

	@Before
	public synchronized void createDatabaseRepresentation() throws SQLException, IOException {
		MockitoAnnotations.openMocks(this);
		if (database == null) {
			doCreateDatabaseRepresentation();
		}
	}

	private void doCreateDatabaseRepresentation() throws SQLException, IOException {
		String[] args = { "-t", "src/test/resources/integrationTesting/dbTypes/h2memory", "-db", "pkorder", "-s",
				"pkorder", "-o", "target/testout/integrationtesting/h2/pkorder", "-u", "sa" };
		given(arguments.getOutputDirectory()).willReturn(new File("target/testout/integrationtesting/h2/pkorder"));
		given(arguments.getDatabaseType()).willReturn("src/test/resources/integrationTesting/dbTypes/h2memory");
		given(arguments.getUser()).willReturn("sa");
		given(arguments.getCatalog()).willReturn(h2MemoryRule.getConnection().getCatalog());
		given(arguments.getSchema()).willReturn(h2MemoryRule.getConnection().getSchema());
		given(arguments.getDatabaseName()).willReturn("pkorder");
		Config config = new Config(args);
		serviceFixture.sqlService().connect(config);
		Database database = new Database(serviceFixture.sqlService().getDbmsMeta(), arguments.getDatabaseName(),
				arguments.getCatalog(), arguments.getSchema());
		serviceFixture.databaseService().gatherSchemaDetails(config, database, null, progressListener);
		H2PrimaryKeySeqnoIT.database = database;
	}

	@Test
	public void primaryKeysShouldBeInCorrectOrder() {
		List<String> pkcolumnNames = database.getTablesMap().get("TABLE1").getPrimaryColumns().stream()
				.map(TableColumn::getName).collect(Collectors.toList());
		assertThat(pkcolumnNames).containsExactly("ZZ", "AA", "BB");
	}
}
