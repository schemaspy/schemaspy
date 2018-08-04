/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.view;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

public class HtmlMainIndexPageTest {

    @Test
    public void whatsTheResultOfWeirdCode() {
        Collection<String> tableName = new ArrayList<>();
        tableName.add("local");

        Collection<String> remotes = new ArrayList<>();
        remotes.add("remote");
        // sort tables and remotes by name
        Collection<String> tmp = new TreeSet<>();
        tmp.addAll(tableName);
        tableName = tmp;
        tmp = new TreeSet<>();
        tmp.addAll(remotes);

        for(String table : tableName) {
            System.out.println(table);
        }
    }

}