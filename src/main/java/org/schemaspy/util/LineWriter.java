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

import java.io.*;

/**
 * BufferedWriter that adds a <code>writeln()</code> method
 * to output a <i>lineDelimited</i> line of text without
 * cluttering up code.
 *
 * @author John Currier
 * @author Daniel Watt
 */
public class LineWriter extends BufferedWriter {
    private final Writer out;

    public LineWriter(File file, String charset) throws UnsupportedEncodingException, FileNotFoundException {
        this(new FileOutputStream(file), charset);
    }

    public LineWriter(File file, int sz, String charset) throws IOException {
        this(new FileOutputStream(file), sz, charset);
    }

    private LineWriter(OutputStream out, String charset) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(out, charset), 8192);
    }

    private LineWriter(OutputStream out, int sz, String charset) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(out, charset), sz);
    }

    private LineWriter(Writer out, int sz) {
        // by this point a charset has already been specified
        super(out, sz);
        this.out = out;
    }

    public void writeln(String str) throws IOException {
        write(str);
        newLine();
    }

    public void writeln() throws IOException {
        newLine();
    }

    /**
     * Intended to simplify use when wrapping StringWriters.
     */
    @Override
    public String toString() {
        try {
            flush();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }

        return out.toString();
    }
}