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
package org.schemaspy.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.schemaspy.input.dbms.service.keywords.Sql92Keywords;
import org.schemaspy.model.Table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlAnalyzerTest {

    private static Table createTable(String container, String name) {
        Table table = mock(Table.class);
        when(table.getContainer()).thenReturn(container);
        when(table.getName()).thenReturn(name);
        return table;
    }

    @Test
    void willIdentifySQLServerQuotedTables() {
        List<Table> tables = Arrays.asList(
                createTable("htmlit", "group"),
                createTable("htmlit", "user"),
                createTable("htmlit", "resources"),
                createTable("htmlit", "group_resources")
        );
        SqlAnalyzer sqlAnalyzer = new SqlAnalyzer(
            "",
            new Sql92Keywords().value(),
            tables,
            Collections.emptyList()
        );


        String viewDefinition = "CREATE VIEW htmlit.userAndGroup AS SELECT u.name AS UserName, g.name AS GroupName FROM [htmlit].[user] u JOIN [htmlit].[group] g ON u.groupId = g.groupId;";
        Set<Table> referenced = sqlAnalyzer.getReferencedTables(viewDefinition);
        assertThat(referenced).extracting(Table::getName).containsExactlyInAnyOrder("user", "group");
    }
}
