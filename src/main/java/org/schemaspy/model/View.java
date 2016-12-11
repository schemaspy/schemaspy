/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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
package org.schemaspy.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.schemaspy.Config;

/**
 * Treat views as tables that have no rows and are represented by the SQL that
 * defined them.
 */
public class View extends Table {
    private String viewSql;

    /**
     * @param db
     * @param catalog
     * @param schema
     * @param name
     * @param remarks
     * @param viewSql
     * @throws SQLException
     */
    public View(Database db, String catalog, String schema,
                String name, String remarks, String viewSql) throws SQLException {
        super(db, catalog, schema, name, remarks);

        //if (viewSql == null)
        //    viewSql = fetchViewSql();

        if (viewSql != null && viewSql.trim().length() > 0)
            this.viewSql = viewSql;
    }

    /**
     * @return
     */
    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public String getViewSql() {
        return viewSql;
    }

}
