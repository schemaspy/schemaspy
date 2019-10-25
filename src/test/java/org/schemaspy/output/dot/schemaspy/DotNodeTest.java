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
package org.schemaspy.output.dot.schemaspy;

import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.Config;
import org.schemaspy.DotConfigUsingConfig;
import org.schemaspy.model.*;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.testing.ConfigRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Nils Petzaell
 */
public class DotNodeTest {

    @Rule
    public ConfigRule configRule = new ConfigRule();

    @Test
    public void escapeHtml() {
        Database database = mock(Database.class);
        Table table = new LogicalTable(database, "catalog", "schema", "<table>", "comment");
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        DotNode dotNode = new DotNode(table,false, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("tooltip=\"&lt;table&gt;");
    }

    @Test
    public void urlEncodeLink() {
        Database database = mock(Database.class);
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        DotNode dotNode = new DotNode(table,false, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("URL=\"a%20table.html\"");
    }

    @Test
    public void htmlEscapeShortType() {
        Database database = mock(Database.class);
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn tableColumn = new TableColumn(table);
        tableColumn.setName("<A>");
        tableColumn.setShortType("<T>");
        tableColumn.setDetailedSize(null);
        table.getColumnsMap().put("<A>", tableColumn);
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        DotNode dotNode = new DotNode(table,false, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("<TD PORT=\"&lt;A&gt;.type\" ALIGN=\"LEFT\">&lt;t&gt;</TD>");
    }

    @Test
    public void htmlEscapeDetailedSize() {
        Database database = mock(Database.class);
        Table table = new LogicalTable(database, "catalog", "schema", "a table", "comment");
        TableColumn tableColumn = new TableColumn(table);
        tableColumn.setName("<A>");
        tableColumn.setShortType("<T>");
        tableColumn.setDetailedSize("<D>");
        table.getColumnsMap().put("<A>", tableColumn);
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        DotNode dotNode = new DotNode(table,false, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("[&lt;D&gt;]");
    }

    @Test
    public void urlForRemoteInRootNonRelative() {
        Database database = mock(Database.class);
        Table table = new RemoteTable(database, "catalog", "schema", "a table", "remote");
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        Config.getInstance().setOneOfMultipleSchemas(true);
        DotNode dotNode = new DotNode(table,true, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("URL=\"../schema/tables/a%20table.html\"");
    }

    @Test
    public void urlForRemoteNotInRootNonRelative() {
        Database database = mock(Database.class);
        Table table = new RemoteTable(database, "catalog", "schema", "a table", "remote");
        DotConfig dotConfig = new DotConfigUsingConfig(Config.getInstance(), false);
        Config.getInstance().setOneOfMultipleSchemas(true);
        DotNode dotNode = new DotNode(table,false, new DotNodeConfig(true, true), dotConfig);
        assertThat(dotNode.toString()).contains("URL=\"../../schema/tables/a%20table.html\"");
    }
}