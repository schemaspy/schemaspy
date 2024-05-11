/*
 * Copyright (C) 2017 Nils Petzaell
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
package org.schemaspy.input.dbms.xml;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

/**
 * @author Nils Petzaell
 */
class SchemaMetaTest {

    @RegisterExtension
    public static LogbackExtension logback = new LogbackExtension();

    @Test
    @Logback(CmFacade.class)
    void deprecationWarningRemarksInTable() {
        logback.expect(Matchers.containsString("deprecated"));
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInTable.xml", "dbname", "schemaName", false);
    }

    @Test
    @Logback(CmFacade.class)
    void deprecationWarningRemarksInColumn() {
        logback.expect(Matchers.containsString("deprecated"));
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/remarksInColumn.xml", "dbname", "schemaName", false);
    }

    @Test
    @Logback(CmFacade.class)
    void okCommentsInTable() {
        logback.expect(Matchers.not(Matchers.containsString("deprecated")));
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInTable.xml", "dbname", "schemaName", false);
    }

    @Test
    @Logback(CmFacade.class)
    void okCommentsInColumn() {
        logback.expect(Matchers.not(Matchers.containsString("deprecated")));
        SchemaMeta schemaMeta = new SchemaMeta("src/test/resources/model/xml/commentsInColumn.xml", "dbname", "schemaName", false);
    }

}
