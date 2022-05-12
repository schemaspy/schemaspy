package org.schemaspy.input.dbms.service;

import org.schemaspy.Config;

import java.time.Clock;

public class DatabaseServiceFactory {

    private final Clock clock = Clock.systemDefaultZone();
    private final SqlService sqlService;

    public DatabaseServiceFactory(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public DatabaseService simple(Config config) {
        return new DatabaseService(
                clock,
                sqlService,
                new TableService(
                        sqlService,
                        new ColumnService(
                                sqlService,
                                config.getIndirectColumnExclusions(),
                                config.getColumnExclusions()
                        ),
                        new IndexService(sqlService)
                ),
                new ViewService(
                        sqlService,
                        new ColumnService(
                                sqlService,
                                config.getIndirectColumnExclusions(),
                                config.getColumnExclusions()
                        )
                ),
                new RoutineService(sqlService, config.getDbProperties()),
                new SequenceService(sqlService, config.getDbProperties())
        );
    }
}
