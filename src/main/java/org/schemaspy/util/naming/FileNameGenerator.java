/*
 * Copyright (C) 2020 Nils Petzaell
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
package org.schemaspy.util.naming;

import org.schemaspy.output.dot.schemaspy.name.Name;

public class FileNameGenerator implements Name {

    private final String original_name;

    public FileNameGenerator(final String original_name) {
        this.original_name = original_name;
    }

    @Override
    public String value() {
        String name = original_name.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
        if (name.length() <= 40 && original_name.equalsIgnoreCase(name)) {
            return name;
        } else {
            String hashAsHex = Integer.toHexString(original_name.hashCode());
            int targetLength = Math.min(39 - hashAsHex.length(), name.length());
            return name.substring(0, targetLength) + "_" + hashAsHex;
        }
    }
}
