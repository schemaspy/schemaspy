/*
 * Copyright (C) 2011 John Currier
 * Copyright (C) 2017 Mårten Bohlin
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

import java.util.Comparator;

/**
 * Metadata about a type
 *
 * @author Samuel Dussault
 */
public class Type implements Comparable<Type> {
    private final String typeOfType;
    private final String catalog;
    private final String schema;
    private final String name;
    private final String description;
    private final String definition;

    private static final Comparator<String> stringComparatorIgnoreCase = Comparator.nullsFirst(String::compareToIgnoreCase);
    private static final Comparator<Type> typeComparator = Comparator
            .comparing(Type::getTypeOfType, stringComparatorIgnoreCase)
            .thenComparing(Type::getCatalog, stringComparatorIgnoreCase)
            .thenComparing(Type::getSchema, stringComparatorIgnoreCase)
            .thenComparing(Type::getName, stringComparatorIgnoreCase);

    /**
     * @param typeOfType
     * @param catalog
     * @param schema
     * @param name
     * @param description
     * @param definition
     */
    public Type(String typeOfType, String catalog, String schema, String name, String description, String definition) {
        this.typeOfType = typeOfType;
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.description = description;
        this.definition = definition;
    }

    /**
     * @return The type of the type (e.g. Domains or Composite)
     */
    public String getTypeOfType() {
        return typeOfType;
    }

    /**
     * @return The type catalog
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * @return The type schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @return The type name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The type definition
     */
    public String getDefinition() {
        return definition;
    }

    /** Compare the type's type, schema, catalog and type while ignoring the case
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Type other) {
        return typeComparator.compare(this, other);
    }
}
