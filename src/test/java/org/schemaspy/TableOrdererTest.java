package org.schemaspy;

import org.junit.Test;
import org.schemaspy.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableOrdererTest {

    private static final String CATALOG = "catalog";
    private static final String SCHEMA = "schema";

    @Test
    public void validateSorting() {
        Database database = mockDatabase("database");
        Table parent = createParent(database);

        Table child = createChild(database);

        parent.getForeignKeysMap().put("cid_fk",new ForeignKeyConstraint(child.getColumnsMap().get("cid"),parent.getColumnsMap().get("cid")));

        Table remote = new RemoteTable(database, CATALOG, SCHEMA, "remote", "baseContainer");

        Table unattached = new LogicalTable(database, CATALOG, SCHEMA, "unattached", "unattached");

        Table recursion = createRecursion(database);

        Table complexRecursion1 = createComplexRecursion1(database);
        Table complexRecursion2 = createComplexRecursion2(database);

        complexRecursion1.getForeignKeysMap().put("c2id_fk", new ForeignKeyConstraint(complexRecursion2.getColumnsMap().get("c1id"), complexRecursion1.getColumnsMap().get("c1id")));
        complexRecursion2.getForeignKeysMap().put("c1id_fk", new ForeignKeyConstraint(complexRecursion1.getColumnsMap().get("c2id"), complexRecursion2.getColumnsMap().get("c2id")));

        List<Table> tables = new ArrayList<>();
        tables.add(remote);
        tables.add(unattached);
        tables.add(recursion);
        tables.add(complexRecursion2);
        tables.add(complexRecursion1);
        tables.add(child);
        tables.add(parent);

        TableOrderer tableOrderer = new TableOrderer();
        List<ForeignKeyConstraint> recursiveConstraints = new ArrayList<>();
        List<Table> orderedByInsert = tableOrderer.getTablesOrderedByRI(tables, recursiveConstraints);
        assertThat(orderedByInsert).containsExactly(child, complexRecursion1, complexRecursion2, recursion, parent, unattached);
        assertThat(recursiveConstraints.size()).isEqualTo(3);
    }

    private Table createParent(Database database) {
        Table parent = new LogicalTable(database, CATALOG, SCHEMA, "parent", "parent");
        TableColumn pid = new TableColumn(parent);
        pid.setName("pid");
        addColumnToTable(pid, parent);
        TableColumn cid = new TableColumn(parent);
        cid.setName("cid");
        addColumnToTable(cid, parent);
        return parent;
    }

    private Table createChild(Database database) {
        Table child = new LogicalTable(database, CATALOG, SCHEMA, "child", "child");
        TableColumn cid = new TableColumn(child);
        cid.setName("cid");
        addColumnToTable(cid, child);
        return child;
    }

    private Table createRecursion(Database database) {
        Table recursion = new LogicalTable(database, CATALOG, SCHEMA, "recursion", "recursion");
        TableColumn rid = new TableColumn(recursion);
        rid.setName("rid");
        addColumnToTable(rid, recursion);
        TableColumn rpid = new TableColumn(recursion);
        rpid.setName("rpid");
        addColumnToTable(rpid, recursion);
        TableColumn rcid = new TableColumn(recursion);
        rcid.setName("rcid");
        addColumnToTable(rcid, recursion);
        recursion.getForeignKeysMap().put("hir_cont_p", new ForeignKeyConstraint(rid, rpid));
        recursion.getForeignKeysMap().put("hir_cont_c", new ForeignKeyConstraint(rcid, rid));
        return recursion;
    }

    private Table createComplexRecursion1(Database database) {
        Table recursion1 = new LogicalTable(database, CATALOG, SCHEMA, "complexRecursion1", "complexRecursion1");
        TableColumn c1id = new TableColumn(recursion1);
        c1id.setName("c1id");
        addColumnToTable(c1id, recursion1);
        TableColumn c2id = new TableColumn(recursion1);
        c2id.setName("c2id");
        addColumnToTable(c2id, recursion1);
        return recursion1;
    }

    private Table createComplexRecursion2(Database database) {
        Table recursion2 = new LogicalTable(database, CATALOG, SCHEMA, "complexRecursion2", "complexRecursion2");
        TableColumn c2id = new TableColumn(recursion2);
        c2id.setName("c2id");
        addColumnToTable(c2id, recursion2);
        TableColumn c1id = new TableColumn(recursion2);
        c1id.setName("c1id");
        addColumnToTable(c1id, recursion2);
        return recursion2;
    }

    private void addColumnToTable(TableColumn tableColumn, Table table) {
        table.getColumnsMap().put(tableColumn.getName(), tableColumn);
    }


    private Database mockDatabase(String name) {
        Database db  = mock(Database.class);
        when(db.getName()).thenReturn(name);
        return db;
    }

}