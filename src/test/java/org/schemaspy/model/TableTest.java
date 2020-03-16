/*
 * Copyright (C) 2020 Nils Petzaell
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableTest {

    private static final Database database = mock(Database.class);

    static {
        when(database.getName()).thenReturn("DBNAME");
    }

    @Test
    public void containerIsSchema() {
        Table table = new Table(database, "CATNAME", "SNAME", "table", null);
        assertThat(table.getContainer()).isEqualToIgnoringCase("SNAME");
    }

    @Test
    public void containerIsCatalog() {
        Table table = new Table(database, "CATNAME", null, "table", null);
        assertThat(table.getContainer()).isEqualToIgnoringCase("CATNAME");
    }

    @Test
    public void containerIsDatabaseName() {
        Table table = new Table(database, null, null, "table", null);
        assertThat(table.getContainer()).isEqualToIgnoringCase("DBNAME");
    }

}