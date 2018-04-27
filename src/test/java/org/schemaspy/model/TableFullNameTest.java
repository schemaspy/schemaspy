/*
 * Copyright (C) 2017 Thomas Traude
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
package org.schemaspy.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Thomas Traude
 */
@RunWith(Parameterized.class)
public class TableFullNameTest {

    @Parameter
    public String db;

    @Parameter(1)
    public String catalog;

    @Parameter(2)
    public String schema;

    @Parameter(3)
    public String table;

    @Parameter(4)
    public String expectedFullName;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //@formatter:off
                //db        catalog         schema      table       expectedFullName
                {null,      null,           null,       null,       "null.null"},        //TODO this is obviously not a realistic valid case, consider checking parameter db for null

                {"myDB",    "myCatalog",    "mySchema", "myTable",  "myCatalog.mySchema.myTable"},

                {null,      "myCatalog",    "mySchema", "myTable",  "myCatalog.mySchema.myTable"},
                {"myDB",    null,           "mySchema", "myTable",  "mySchema.myTable"},
                {"myDB",    "myCatalog",    null,       "myTable",  "myCatalog.myTable"},
                {null,      "myCatalog",    "mySchema", null,       "myCatalog.mySchema.null"}, //TODO this is obviously another unrealstic case, table name should not be null

                {"myDB",    null,           null,       "myTable",  "myDB.myTable"},
                //@formatter:on
        });
    }

    @Test
    public void testFullName() {
        String actualFullName = Table.getFullName(db, catalog, schema, table);
        assertThat(actualFullName, is(expectedFullName));
    }
}