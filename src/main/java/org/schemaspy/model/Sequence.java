/*
 * Copyright (C) 2019 AE Ibrahim
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

public class Sequence implements Comparable<Sequence> {
    private final String name;
    private final Integer startValue;
    private final Integer increment;

    public Sequence(String name, Integer startValue, Integer increment) {
        this.name = name;
        this.startValue = startValue;
        this.increment = increment;
    }

    public String getName() {
        return name;
    }

    public Integer getStartValue() {
        return startValue;
    }

    public Integer getIncrement() {
        return increment;
    }

    @Override
    public int compareTo(Sequence other) {
        int rc = getName().compareTo(other.getName());
        if (rc == 0)
            rc = getStartValue().compareTo(other.getStartValue());
        if (rc == 0)
            rc = getIncrement().compareTo(other.getIncrement());
        return rc;
    }
}
