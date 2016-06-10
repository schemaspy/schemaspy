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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Format a LogRecord into a single concise line.
 *
 * @author John Currier
 */
public class LogFormatter extends Formatter {
    private final String lineSeparator = System.getProperty("line.separator");
    private final int MAX_LEVEL_LEN = 7;
    private static final String formatSpec = "HH:mm:ss.";
    private static final long serialVersionUID = 1L;

    /**
     * Date formatter for time-to-text translation.
     * These are very expensive to create and not thread-safe, so do it once per thread.
     */
    private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>()
    {
        @Override
        public DateFormat initialValue()
        {
            SimpleDateFormat formatter = new SimpleDateFormat(formatSpec);
            return formatter;
        }
    };

    /**
     * Optimization to keep from creating a new {@link java.util.Date} for every call to
     * {@link #toString()}.
     */
    private static final ThreadLocal<Date> date = new ThreadLocal<Date>()
    {
        @Override
        public Date initialValue()
        {
            return new Date();
        }
    };

    /**
     * Format the given LogRecord.
     *
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder buf = new StringBuilder(128);

        // format the date portion:

        // thread-safe pseudo-singletons:
        date.get().setTime(record.getMillis());
        buf.append(dateFormatter.get().format(date.get()));

        // compute frac as the number of milliseconds off of a whole second
        long frac = record.getMillis() % 1000;

        // force longFrac to overflow 1000 to give 1 followed by
        // 'leading' zeros
        frac += 1000;

        // append the fraction of a second at the end (w/o leading overflowed 1)
        buf.append(Long.toString(frac).substring(1));

        buf.append(" ");
        StringBuilder level = new StringBuilder(record.getLevel().getLocalizedName());
        if (level.length() > MAX_LEVEL_LEN)
            level.setLength(MAX_LEVEL_LEN);
        level.append(":");
        while (level.length() < MAX_LEVEL_LEN + 1)
            level.append(' ');
        buf.append(level);
        buf.append(" ");

        String name;
        if (record.getSourceClassName() != null) {
            name = record.getSourceClassName();
        } else {
            name = record.getLoggerName();
        }

        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < name.length() - 1)
            name = name.substring(lastDot + 1);
        buf.append(name);

        if (record.getSourceMethodName() != null) {
            buf.append('.');
            buf.append(record.getSourceMethodName());
        }

        buf.append(" - ");
        buf.append(formatMessage(record));
        buf.append(lineSeparator);

        if (record.getThrown() != null) {
            try {
                StringWriter stacktrace = new StringWriter();
                record.getThrown().printStackTrace(new PrintWriter(stacktrace, true));
                buf.append(stacktrace.toString());
                // stack trace already has a line separator
            } catch (Exception ignore) {
            }
        }

        return buf.toString();
    }
}
