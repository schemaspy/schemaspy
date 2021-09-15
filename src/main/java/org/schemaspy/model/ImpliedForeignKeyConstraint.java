/*
 * Copyright (C) 2004 - 2010 John Currier
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

import java.util.List;

/**
 * @author John Currier
 */
public class ImpliedForeignKeyConstraint extends ForeignKeyConstraint {
    /**
     * @param parentColumns
     * @param childColumns
     */
    public ImpliedForeignKeyConstraint(List<TableColumn> parentColumns, List<TableColumn> childColumns) {
        super(parentColumns, childColumns);
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return "Implied Constraint";
    }

    /**
     * @return
     */
    @Override
    public boolean isImplied() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(toString(getChildTable(), getChildColumns()));
        buf.append("'s name implies that it's a child of ");
        buf.append(toString(getParentTable(), getParentColumns()));
        buf.append(", but it doesn't reference that column.");
        return buf.toString();
    }
}
