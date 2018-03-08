package org.schemaspy.service;

import org.junit.Test;
import org.schemaspy.cli.CommandLineArguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableServiceTest {

    private SqlService sqlService = mock(SqlService.class);
    private CommandLineArguments commandLineArguments = mock(CommandLineArguments.class);

    private TableService tableService = new TableService(sqlService,commandLineArguments);

    private Supplier<Method> isIndexRowMethod = () -> {
        Method m = null;
        try {
            m = TableService.class.getDeclaredMethod("isIndexRow", ResultSet.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        m.setAccessible(true);
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