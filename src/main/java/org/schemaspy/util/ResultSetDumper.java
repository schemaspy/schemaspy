/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2011 John Currier
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Simple diagnostic class to give a textual representation
 * of columns pointed to by a result set.
 * 
 * @author John Currier
 */
public class ResultSetDumper {
    public static String dump(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int numCols = meta.getColumnCount();
        StringBuilder buf = new StringBuilder();

        for (int col = 1; col <= numCols; ++col) {
            buf.append(meta.getColumnLabel(col));
            buf.append("=");
            String value = rs.getString(col);
            if (value != null)
                buf.append('\'');
            buf.append(value);
            if (value != null)
                buf.append('\'');
            if (col < numCols)
                buf.append(", ");
        }
        
        return buf.toString();
    }

    public static void dumpAll(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int row = 1;
        while (rs.next()) {
            System.out.println("### ROW: " + row);
            for (int i = 1; i <= columnsNumber; i++) {
                System.out.println(rsmd.getColumnName(i) + ": " + rs.getString(i));
            }
            row++;
        }
    }
}
