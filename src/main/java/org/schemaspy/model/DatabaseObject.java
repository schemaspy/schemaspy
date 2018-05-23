/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
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
package org.schemaspy.model;

import java.util.Set;

/**
 * Created by rkasa on 2016-04-15.
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 */
public class DatabaseObject implements Comparable<DatabaseObject>{
    private String name;
    private final String fullName;
    private final String typeName;
    private final Integer type;
    private final int length;
    private final Set<TableColumn> parents;
    private final Set<TableColumn> children;

    public DatabaseObject(TableColumn object) {
        this.name = object.getName();
        this.fullName = object.getTable().getFullName() + "." + object.getName();
        this.typeName = object.getTypeName();
        this.type = object.getType();
        this.length = object.getLength();
        this.parents = object.getParents();
        this.children = object.getChildren();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public Set<TableColumn> getParents() {
        return parents;
    }

    public Set<TableColumn> getChildren() {
        return children;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return name;
    }
    
	@Override
	public int compareTo(DatabaseObject column2) {
        int rc = this.getFullName().compareToIgnoreCase(column2.getFullName());
        if (rc == 0) {
            if (this.getType() != null && column2.getType() != null)
            	// type is exact while typeName can be adorned with additional stuff (e.g. MSSQL appends " identity" for auto-inc keys)
            	rc = this.getType().compareTo(column2.getType());
            else
                rc = this.getTypeName().compareToIgnoreCase(column2.getTypeName());
        }
        if (rc == 0)
            rc = this.getLength() - column2.getLength();
        return rc;
	}

}
