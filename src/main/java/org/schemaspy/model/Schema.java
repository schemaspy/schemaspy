/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2017 Ismail Simsek
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

import java.util.Objects;

/**
 * @author John Currier
 * @author Ismail Simsek
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public final class Schema implements Comparable<Schema>{
	private final String name;
	private String comment =null;
    
	public Schema(String name) {
		this(name,null);
	}

	public Schema(String name, String comment) {
		this.name = Objects.requireNonNull(name);
		this.comment = comment;
	}

	public String getName() {
		return name;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

    public int compareTo(Schema i) {
    	return this.getName().compareTo(i.getName());
    }

    public String toString() {
        return name;
    }

	@Override
	public boolean equals(Object o) {
		if (o instanceof Schema) {
			return ((Schema)o).getName().equals(name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
