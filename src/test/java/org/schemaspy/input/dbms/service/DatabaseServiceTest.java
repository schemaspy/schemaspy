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
package org.schemaspy.input.dbms.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseServiceTest {

    private static final ProgressListener progressListener = mock(ProgressListener.class);

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    private Instant currentTime = Instant.now();
    private final Clock clock = mock(Clock.class);

    private static final Pattern DEFAULT_TABLE_INCLUSION = Pattern.compile(".*"); // match everything
    private static final Pattern DEFAULT_TABLE_EXCLUSION = Pattern.compile(".*\\$.*");

    @BeforeEach
    public void setup() {
        when(clock.instant()).thenAnswer(invocation -> currentTime);
    }

    @Test
    @Logback(DatabaseService.class)
    void databaseServicePrintsInformationWhenConnectionTablesWillTakeMoreThan30MinutesAndExportedKeysIsEnabled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logback.expect(Matchers.containsString("Estimated time remaining"));
        SqlService sqlService = mock(SqlService.class);
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(31, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        RoutineService routineService = mock(RoutineService.class);
        SequenceService sequenceService = mock(SequenceService.class);
        DatabaseService databaseService = new DatabaseService(
                clock,
                sqlService,
                true,
                DEFAULT_TABLE_INCLUSION,
                DEFAULT_TABLE_EXCLUSION,
                1,
                true,
                true,
                new Properties(),
                tableService,
                viewService,
                routineService,
                sequenceService
        );
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);
    }

    @Test
    @Logback(DatabaseService.class)
    void databaseServiceDoesNotPrintInformationWhenConnectionTablesWillTakeMoreThan30MinutesAndExportedKeysIsDisabled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logback.expect(Matchers.not(Matchers.containsString("Estimated time remaining")));
        SqlService sqlService = mock(SqlService.class);
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(31, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        RoutineService routineService = mock(RoutineService.class);
        SequenceService sequenceService = mock(SequenceService.class);
        DatabaseService databaseService = new DatabaseService(
                clock,
                sqlService,
                true,
                DEFAULT_TABLE_INCLUSION,
                DEFAULT_TABLE_EXCLUSION,
                1,
                false,
                true,
                new Properties(),
                tableService,
                viewService,
                routineService,
                sequenceService
        );
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);
    }

    @Test
    @Logback(DatabaseService.class)
    void databaseServiceDoesNotPrintInformationWhenConnectionTablesWillTakeLessThan30Minutes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logback.expect(Matchers.not(Matchers.containsString("Estimated time remaining")));
        SqlService sqlService = mock(SqlService.class);
        TableService tableService = mock(TableService.class);
        doAnswer(invocation -> {
            currentTime = currentTime.plus(1, ChronoUnit.MINUTES);
            return null;
        }).when(tableService).connectForeignKeys(any(),any(),anyMap());
        ViewService viewService = mock(ViewService.class);
        RoutineService routineService = mock(RoutineService.class);
        SequenceService sequenceService = mock(SequenceService.class);
        DatabaseService databaseService = new DatabaseService(
                clock,
                sqlService,
                true,
                DEFAULT_TABLE_INCLUSION,
                DEFAULT_TABLE_EXCLUSION,
                1,
                true,
                true,
                new Properties(),
                tableService,
                viewService,
                routineService,
                sequenceService
        );
        List<Table> tablesList = new ArrayList<>();
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        tablesList.add(mock(Table.class));
        Database database = mock(Database.class);
        when(database.getTables()).thenReturn(tablesList);
        Method connectTables = DatabaseService.class.getDeclaredMethod("connectTables", Database.class, ProgressListener.class);
        connectTables.setAccessible(true);

        connectTables.invoke(databaseService, database, progressListener);
    }

}