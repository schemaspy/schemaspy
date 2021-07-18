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

import java.time.Clock;

import org.schemaspy.input.dbms.service.ColumnService;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.IndexService;
import org.schemaspy.input.dbms.service.RoutineService;
import org.schemaspy.input.dbms.service.SequenceService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.service.TableService;
import org.schemaspy.input.dbms.service.ViewService;

/**
 * @author Nils Petzaell
 */
public final class ServiceFixture {
	private final SqlService sqlService = new SqlService();

	private final DatabaseService databaseService;

	public ServiceFixture() {
		final ColumnService columnService = new ColumnService(sqlService);
		final TableService tableService = new TableService(sqlService, columnService, new IndexService(sqlService));
		final ViewService viewService = new ViewService(sqlService, columnService);
		this.databaseService = new DatabaseService(Clock.systemDefaultZone(), sqlService, tableService, viewService,
				new RoutineService(sqlService), new SequenceService(sqlService));
	}

	public SqlService getSqlService() {
		return sqlService;
	}

	public DatabaseService getDatabaseService() {
		return databaseService;
	}

}
