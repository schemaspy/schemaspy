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
package org.schemaspy.service;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nils Petzaell
 */
public class TableServiceIsIndexRowTest {

    private SqlService sqlService = mock(SqlService.class);

    private TableService tableService = new TableService(sqlService);

    private Supplier<Method> isIndexRowMethod = () -> {
        Method m = null;
        try {
            m = TableService.class.getDeclaredMethod("isIndexRow", ResultSet.class);
            m.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Method finalM = m;
        isIndexRowMethod = () -> finalM;
        return m;
    };

    private boolean isIndexRow(ResultSet rs) throws InvocationTargetException, IllegalAccessException {
        return (Boolean)isIndexRowMethod.get().invoke(tableService, rs);
    }

    private ResultSet newResultSet(short type, int ordinal) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getShort("TYPE")).thenReturn(type);
        when(rs.getShort("ORDINAL_POSITION")).thenReturn((short) ordinal);
        return rs;
    }

    @Test
    public void TableStatAndOrdinalZeroIsNotIndexRow() throws InvocationTargetException, IllegalAccessException, SQLException {
        ResultSet rs = newResultSet(DatabaseMetaData.tableIndexStatistic, 0);
        assertThat(isIndexRow(rs)).isFalse();
    }

    @Test
    public void NotTableStatAndOrdinalZeroIsNotIndexRow() throws SQLException, InvocationTargetException, IllegalAccessException {
        ResultSet rs = newResultSet(DatabaseMetaData.tableIndexClustered, 0);
        assertThat(isIndexRow(rs)).isFalse();
    }

    @Test
    public void TableStatAndOrdinalNotZeroIsNotIndexRow() throws SQLException, InvocationTargetException, IllegalAccessException {
        ResultSet rs = newResultSet(DatabaseMetaData.tableIndexStatistic, 10);
        assertThat(isIndexRow(rs)).isFalse();
    }

    @Test
    public void NotTableStatsAndOrdinalNotZeroIsIndexRow() throws SQLException, InvocationTargetException, IllegalAccessException {
        ResultSet rs = newResultSet(DatabaseMetaData.tableIndexClustered,10);
        assertThat(isIndexRow(rs)).isTrue();
    }

}