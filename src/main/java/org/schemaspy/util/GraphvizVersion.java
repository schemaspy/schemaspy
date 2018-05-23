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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Simple class that allows logical comparisons between "dotted" versions of products.
 *
 * e.g. version 2.1.4 should be less than version 2.1.10.
 *
 * @author John Currier
 * @author Daniel Watt
 * @version 1.0
 */
public class GraphvizVersion implements Comparable<GraphvizVersion> {
    private final List<Integer> segments = new ArrayList<Integer>();
    private final String asString;
    private final int hashCode;

    public GraphvizVersion(String version) {
        asString = version;
        int hash = 0;
        if (version != null) {
            StringTokenizer tokenizer = new StringTokenizer(version, ". -_");

            while (tokenizer.hasMoreTokens()) {
                Integer segment = new Integer(tokenizer.nextToken());
                segments.add(segment);
                hash += segment;
            }
        }

        hashCode = hash;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     */
    public int compareTo(GraphvizVersion other) {
        int size = Math.min(segments.size(), other.segments.size());
        for (int i = 0; i < size; ++i) {
            Integer thisSegment = segments.get(i);
            Integer otherSegment = other.segments.get(i);
            int result = thisSegment.compareTo(otherSegment);
            if (result != 0)
                return result;
        }

        if (segments.size() == other.segments.size())
            return 0;
        if (segments.size() > other.segments.size())
            return 1;
        return -1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof GraphvizVersion))
            return false;
        return compareTo((GraphvizVersion)other) == 0;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return asString;
    }
}