package org.schemaspy.input.dbms.service;

import org.schemaspy.Config;

import java.time.Clock;

public class DatabaseServiceFactory {

    private final Clock clock = Clock.systemDefaultZone();
    private final SqlService sqlService;

    public DatabaseServiceFactory(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public DatabaseService forSingleSchema(Config config) {
        return create(config, false);
    }

    public DatabaseService forMultipleSchemas(Config config) {
        return create(config, true);
    }

    private DatabaseService create(Config config, boolean multipleSchemas) {
        return new DatabaseService(
                clock,
                sqlService,
                config.isViewsEnabled(),
                config.getTableInclusions(),
                config.getTableExclusions(),
                config.getMaxDbThreads(),
                config.isExportedKeysEnabled(),
                config.isNumRowsEnabled(),
                config.getDbProperties(),
                new TableService(
                        sqlService,
                        config.isExportedKeysEnabled(),
                        multipleSchemas,
                        config.getTableInclusions(),
                        config.getTableExclusions(),
                        config.getDbProperties(),
                        new ColumnService(
                                sqlService,
                                config.getIndirectColumnExclusions(),
                                config.getColumnExclusions()
                        ),
                        new IndexService(sqlService, config.getDbProperties())
                ),
                new ViewService(
                        sqlService,
                        config.getDbProperties(),
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
