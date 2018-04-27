/*
 * Copyright (C) 2004 - 2010 John Currier
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
package org.schemaspy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link HashMap} implementation that uses {@link String}s as its keys
 * where the keys are treated without regard to case.  That is, <code>get("MyTableName")</code>
 * will return the same object as <code>get("MYTABLENAME")</code>.
 *
 * @author John Currier
 * @author Daniel Watt
 */
public class CaseInsensitiveMap<V> extends HashMap<String, V>
{
    private static final long serialVersionUID = 1L;

    public CaseInsensitiveMap()
    {
    }

    public CaseInsensitiveMap(int initialCapacity)
    {
        super(initialCapacity);
    }

    @Override
    public V get(Object key) {
        V name = null;
        if (key != null) {
            name = super.get(((String)key).toUpperCase());
        }
        return name;
    }

    @Override
    public V put(String key, V value) {
        return super.put(key.toUpperCase(), value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        for (Entry<? extends String, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        return super.remove(((String)key).toUpperCase());
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(((String)key).toUpperCase());
    }
}