/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Daniel Watt
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

import org.schemaspy.DbAnalyzer;

/**
 * See {@link DbAnalyzer#getRailsConstraints(java.util.Map)} for
 * details on Rails naming conventions.
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Thomas Traude
 * @author Daniel Watt
 */
public class RailsForeignKeyConstraint extends ForeignKeyConstraint {
    /**
     * @param parentColumn
     * @param childColumn
     */
    public RailsForeignKeyConstraint(TableColumn parentColumn, TableColumn childColumn) {
        super(parentColumn, childColumn);
    }

    /**
     * Normally the name of the constraint, but this one is implied by
     * Rails naming conventions.
     *
     * @return
     */
    @Override
    public String getName() {
        return "ByRailsConventionConstraint";
    }
}