package org.schemaspy.input.dbms.service;

import java.time.Clock;

public class DatabaseServiceFactory {

    private final Clock clock = Clock.systemDefaultZone();
    private final SqlService sqlService;

    public DatabaseServiceFactory(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public DatabaseService simple() {
        return new DatabaseService(
                Clock.systemDefaultZone(),
                sqlService,
                new TableService(sqlService, new ColumnService(sqlService), new IndexService(sqlService)),
                new ViewService(sqlService, new ColumnService(sqlService)),
                new RoutineService(sqlService),
                new SequenceService(sqlService)
        );
    }
}
