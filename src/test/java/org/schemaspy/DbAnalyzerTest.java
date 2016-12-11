package org.schemaspy;

import junit.framework.TestCase;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.schemaspy.model.Database;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rkasa on 2016-12-04.
 */
public class DbAnalyzerTest extends TestCase {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetImpliedConstraints() throws Exception {
        String catalog = "test";
        String schema = "dbo";
        Database database = Mockito.mock(Database.class);

        Table table1 = Mockito.mock(Table.class);
        TableColumn tableColumn1 = Mockito.mock(TableColumn.class);
//        tableColumn1.
//        table1.getColumns().add()

//        Table table1 = new Table(database, catalog, schema, "table1", "");
//        Table table2 = new Table(database, catalog, schema, "table2", "");
//        Table table3 = new Table(database, catalog, schema, "table3", "");
//        Table table4 = new Table(database, catalog, schema, "table4", "");

        List<Table> tables = new ArrayList<Table>();
//        tables.add(table1);
//        tables.add(table2);
//        tables.add(table3);
//        tables.add(table4);

        List<ImpliedForeignKeyConstraint> impliedForeignKeyConstraintList = DbAnalyzer.getImpliedConstraints(tables);
        Assert.assertThat(impliedForeignKeyConstraintList.isEmpty(), Is.is(false));
    }

    @Test
    public void testGetRailsConstraints() throws Exception {
        assertTrue(true);
    }

}