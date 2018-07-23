/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
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

import org.schemaspy.util.CaseInsensitiveMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A read-only map that treats both collections of Tables and Views as one
 * combined collection.
 * This is a bit strange, but it simplifies logic that otherwise treats
 * the two as if they were one collection.
 *
 * @author John Currier
 */
class CombinedMap implements Map<String, Table> {
    private final Map<String, ? extends Table> map1;
    private final Map<String, ? extends Table> map2;

    public CombinedMap(Map<String, ? extends Table> map1, Map<String, ? extends Table> map2)  {
        this.map1 = map1;
        this.map2 = map2;
    }

    @Override
    public Table get(Object name) {
        Table table = map1.get(name);
        if (table == null)
            table = map2.get(name);
        return table;
    }

    @Override
    public int size() {
        return map1.size() + map2.size();
    }

    @Override
    public boolean isEmpty() {
        return map1.isEmpty() && map2.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map1.containsKey(key) || map2.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map1.containsValue(value) || map2.containsValue(value);
    }

    @Override
    public Table put(String name, Table table) {
        throw new UnsupportedOperationException();
    }

    /**
     * Warning: potentially expensive operation
     */
    @Override
    public Set<String> keySet() {
        return getCombined().keySet();
    }

    /**
     * Warning: potentially expensive operation
     */
    @Override
    public Set<Entry<String, Table>> entrySet() {
        return getCombined().entrySet();
    }

    /**
     * Warning: potentially expensive operation
     */
    @Override
    public Collection<Table> values() {
        return getCombined().values();
    }

    private Map<String, Table> getCombined() {
        Map<String, Table> all = new CaseInsensitiveMap<>(size());
        all.putAll(map1);
        all.putAll(map2);
        return all;
    }

    @Override
    public Table remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Table> table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
