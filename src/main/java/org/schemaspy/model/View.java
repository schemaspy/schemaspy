/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Nils Petzaell
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
 * Treat views as tables that have no rows and are represented by the SQL that
 * defined them.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Nils Petzaell
 *
 */
public class View extends Table {
    private String viewDefinition;

    /**
     * @param db
     * @param catalog
     * @param schema
     * @param name
     * @param remarks
     * @param viewDefinition
     */
    public View(Database db, String catalog, String schema,
                String name, String remarks, String viewDefinition) {
        super(db, catalog, schema, name, remarks);

        if (viewDefinition != null && viewDefinition.trim().length() > 0)
            this.viewDefinition = viewDefinition;
    }

    /**
     * @return
     */
    @Override
    public boolean isView() {
        return true;
    }

    @Override
    public String getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(String viewDefinition) {
        this.viewDefinition = viewDefinition;
    }
}
