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

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Nils Petzaell
 */
public class IndexServiceAddPrimaryKeyColumnTest {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    private SqlService sqlService = mock(SqlService.class);

    private IndexService indexService = new IndexService(sqlService);

    private Supplier<Method> addPrimaryKeyColumnMethod = () -> {
        Method m = null;
        try {
            m = IndexService.class.getDeclaredMethod("addPrimaryKeyColumn", Table.class, ResultSet.class);
            m.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Method finalM = m;
        addPrimaryKeyColumnMethod = () -> finalM;
        return m;
    };

    private void addPrimaryKeyColumn(Table table, ResultSet rs) throws InvocationTargetException, IllegalAccessException {
        addPrimaryKeyColumnMethod.get().invoke(indexService, table, rs);
    }

    @Test
    public void addExistingTableColumn() throws InvocationTargetException, IllegalAccessException, SQLException {
        TableColumn tableColumn = mock(TableColumn.class);
        Table table = mock(Table.class);
        when(table.getColumn("aColumn")).thenReturn(tableColumn);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("PK_NAME")).thenReturn("aPKIndex");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("aColumn");

        addPrimaryKeyColumn(table, resultSet);

        verify(table, times(1)).setPrimaryColumn(tableColumn);
    }

    @Test
    @Logger(IndexService.class)
    public void addNonExistingTableColumn() throws SQLException, InvocationTargetException, IllegalAccessException {
        Table table = mock(Table.class);
        when(table.getFullName()).thenReturn("cat.schema.table");

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("PK_NAME")).thenReturn("aPKNoColumn");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("NO");
        when(resultSet.getString("TABLE_CAT")).thenReturn("RE_CAT");
        when(resultSet.getString("TABLE_SCHEM")).thenReturn("RE_SCHEMA");
        when(resultSet.getString("TABLE_NAME")).thenReturn("RE_TABLE");

        addPrimaryKeyColumn(table, resultSet);

        verify(table,never()).setPrimaryColumn(any());

        assertThat(loggingRule.getLog()).contains("RE_CAT.RE_SCHEMA.RE_TABLE.NO");

    }
}