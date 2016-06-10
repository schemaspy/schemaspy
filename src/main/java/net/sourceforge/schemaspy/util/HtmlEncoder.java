/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
package net.sourceforge.schemaspy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple (i.e. 'stupid') class that does a simple mapping between
 * HTML characters and their 'encoded' equivalents.
 *
 * @author John Currier
 */
public class HtmlEncoder {
    private static final Map<String, String> map = new HashMap<String, String>();

    static {
        map.put("<", "&lt;");
        map.put(">", "&gt;");
        map.put("\n", "<br>" + System.getProperty("line.separator"));
        map.put("\r", "");
    }

    private HtmlEncoder() {}

    /**
     * Returns an HTML-encoded equivalent of the specified character.
     *
     * @param ch
     * @return
     */
    public static String encodeToken(char ch) {
        return encodeToken(String.valueOf(ch));
    }

    /**
     * Returns an HTML-encoded equivalent of the specified tokenized string,
     * where tokens such as '<', '>', '\n' and '\r' have been isolated from
     * other tokens.
     *
     * @param str
     * @return
     */
    public static String encodeToken(String str) {
        String result = map.get(str);
        return (result == null) ? str : result;
    }

    /**
     * Returns an HTML-encoded version of the specified string
     *
     * @param str
     * @return
     */
    public static String encodeString(String str) {
        int len = str.length();
        StringBuilder buf = new StringBuilder(len * 2); // x2 should limit # of reallocs
        for (int i = 0; i < len; i++) {
            buf.append(encodeToken(str.charAt(i)));
        }
        return buf.toString();
    }
}
