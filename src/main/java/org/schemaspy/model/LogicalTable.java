/*
 * Copyright (C) 2011 John Currier
 * Copyright (C) 2017 Thomas Traude
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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
package org.schemaspy.model;

/**
 * This is a logical (versus physical) table that represents something
 * that doesn't really exist in the current database.
 *
 * @author John Currier
 * @author Thomas Traude
 */
public class LogicalTable extends Table {
    public LogicalTable(Database db, String catalog, String schema, String name, String comments) {
        super(db, catalog, schema, name, comments);
    }

    /**
     * Don't attempt to query our metadata from the database.
     *
     * @return true
     */
    @Override
    public boolean isLogical() {
        return true;
    }
}
