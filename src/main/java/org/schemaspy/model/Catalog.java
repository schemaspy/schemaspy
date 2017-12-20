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

import java.util.Map;
import java.util.Objects;

import org.schemaspy.util.CaseInsensitiveMap;

public final class Catalog implements Comparable<Catalog>{
    private String name;
    private String comment = null;

	public Catalog(String name) {
		super();
		Objects.requireNonNull(name);
		this.name = name;
	}
	
	public Catalog(String name, String comment) {
		this(name);
		this.name = name;
		this.comment = comment;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
    public int compareTo(Catalog i) {
    	return this.getName().compareTo(i.getName());
    }
    public String toString() {
        return name;
    }

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Catalog catalog = (Catalog) o;
		return Objects.equals(name, catalog.name);
	}

	@Override public int hashCode() {

		return Objects.hash(name);
	}
}
