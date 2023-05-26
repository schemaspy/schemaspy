package org.schemaspy.input.dbms.service;

import org.schemaspy.input.dbms.ProcessingConfig;

import java.time.Clock;

public class DatabaseServiceFactory {

    private final Clock clock = Clock.systemDefaultZone();
    private final SqlService sqlService;

    public DatabaseServiceFactory(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public DatabaseService forSingleSchema(ProcessingConfig processingConfig) {
        return create(processingConfig, false);
    }

    public DatabaseService forMultipleSchemas(ProcessingConfig processingConfig) {
        return create(processingConfig, true);
    }

    private DatabaseService create(ProcessingConfig processingConfig, boolean multipleSchemas) {
        return new DatabaseService(
                clock,
                sqlService,
                processingConfig.isViewsEnabled(),
                processingConfig.getTableInclusions(),
                processingConfig.getTableExclusions(),
                processingConfig.getMaxDbThreads(),
                processingConfig.isExportedKeysEnabled(),
                processingConfig.isNumRowsEnabled(),
                processingConfig.getDatabaseTypeProperties(),
                new TableService(
                        sqlService,
                        processingConfig.isExportedKeysEnabled(),
                        multipleSchemas,
                        processingConfig.getTableInclusions(),
                        processingConfig.getTableExclusions(),
                        processingConfig.getDatabaseTypeProperties(),
                        new ColumnService(
                                sqlService,
                                processingConfig.getIndirectColumnExclusions(),
                                processingConfig.getColumnExclusions()
                        ),
                        new IndexService(sqlService, processingConfig.getDatabaseTypeProperties())
                ),
                new ViewService(
                        sqlService,
                        processingConfig.getDatabaseTypeProperties(),
                        new ColumnService(
                                sqlService,
                                processingConfig.getIndirectColumnExclusions(),
                                processingConfig.getColumnExclusions()
                        )
                ),
                new RoutineService(sqlService, processingConfig.getDatabaseTypeProperties()),
                new TypeService(sqlService, processingConfig.getDatabaseTypeProperties()),
                new SequenceService(sqlService, processingConfig.getDatabaseTypeProperties())
        );
    }
}
