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
package org.schemaspy.service;

import org.schemaspy.model.Table;

import java.sql.SQLException;

/**
 * @author Nils Petzaell
 */
public class ColumnInitializationFailure extends SQLException {
    private static final long serialVersionUID = 1L;

    public ColumnInitializationFailure(Table table, SQLException failure) {
        super("Failed to collect column details for " + (table.isView() ? "view" : "table") + " '" + table.getName() + "' in schema '" + table.getContainer() + "'");
        initCause(failure);
    }
}