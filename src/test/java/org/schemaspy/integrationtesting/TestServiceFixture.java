package org.schemaspy.integrationtesting;

import java.time.Clock;

import org.schemaspy.input.dbms.service.ColumnService;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.IndexService;
import org.schemaspy.input.dbms.service.RoutineService;
import org.schemaspy.input.dbms.service.SequenceService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.service.TableService;
import org.schemaspy.input.dbms.service.ViewService;

public class TestServiceFixture {
	private final Clock clock = Clock.systemDefaultZone();

	private final SqlService sqlService = new SqlService();

	private final ColumnService columnService = new ColumnService(sqlService);
	private final TableService tableService = new TableService(sqlService, columnService, new IndexService(sqlService));
	private final ViewService viewService = new ViewService(sqlService, columnService);
	private final DatabaseService databaseService = new DatabaseService(clock, sqlService, tableService, viewService,
			new RoutineService(sqlService), new SequenceService(sqlService));

	public SqlService sqlService() {
		return sqlService;
	}

	public DatabaseService databaseService() {
		return databaseService;
	}
}
