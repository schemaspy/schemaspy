/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sourceforge.schemaspy.util;

import junit.framework.TestCase;

/**
 * Simple tests to try out Ruby On Rails naming convention detection techniques.
 *
 * @author John Currier
 */
public class RailsNamingTest extends TestCase {
    /**
     * Test Rails naming convention conversion for 'table_id' to 'tables'
     */
    public void testPluralize() {
        // given column name should ref expected table (based on RoR conventions)
        String columnName = "vaccine_id";
        String expectedTableName = "vaccines";

        String singular = columnName.substring(0, columnName.length() - 3);
        String primaryTableName = Inflection.pluralize(singular);

        assertEquals(expectedTableName, primaryTableName);
    }

    /**
     * Test Rails naming convention conversion for multi-word tables
     */
    public void testPluralizeMultiWordTable() {
        // given column name should ref expected table (based on RoR conventions)
        String columnName = "active_ingredient_id";
        String expectedTableName = "active_ingredients";

        String singular = columnName.substring(0, columnName.length() - 3);
        String primaryTableName = Inflection.pluralize(singular);

        assertEquals(expectedTableName, primaryTableName);
    }
}