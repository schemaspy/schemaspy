/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2017 - 2018 Nils Petzaell
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
package org.schemaspy.service.helper;

/**
 * Created by rkasa on 2016-12-10.
 * Collection of fundamental table/view metadata
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class BasicTableMeta
{
    @SuppressWarnings("hiding")
    final String catalog;
    @SuppressWarnings("hiding")
    final String schema;
    final String name;
    final String type;
    final String remarks;
    final String viewDefinition;
    final long numRows;  // -1 if not determined

    /**
     * @param schema
     * @param name
     * @param type typically "TABLE" or "VIEW"
     * @param remarks
     * @param viewDefinition optional textual SQL used to create the view
     * @param numRows number of rows, or -1 if not determined
     */
    public BasicTableMeta(String catalog, String schema, String name, String type, String remarks, String viewDefinition, long numRows)
    {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.remarks = remarks;
        this.viewDefinition = viewDefinition;
        this.numRows = numRows;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public long getNumRows() {
        return numRows;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getType() {
        return type;
    }

    public String getViewDefinition() {
        return viewDefinition;
    }
}