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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation of {@link PasswordReader} that takes advantage of the
 * built-in password reading abilities of Java6 (or higher).
 *
 * Use {@link PasswordReader#getInstance()} to get an instance of
 * PasswordReader that's appropriate for your JVM
 * (this one requires a Java6 or higher JVM).
 *
 * @author John Currier
 */
public class ConsolePasswordReader extends PasswordReader {
    private final Object console;
    private final Method readPassword;

    /**
     * Attempt to resolve the Console methods that were introduced in Java6.
     *
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    protected ConsolePasswordReader() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // get the console by calling System.console() (Java6+ method)
        Method consoleGetter = System.class.getMethod("console", (Class[])null);
        console = consoleGetter.invoke(null, (Object[])null);

        // get Console.readPassword(String, Object[]) method
        Class<?>[] paramTypes = new Class<?>[] {String.class, Object[].class};
        readPassword = console.getClass().getMethod("readPassword", paramTypes);
    }

    /**
     * Attempt to use the previously resolved Console.
     * If unable to use it then revert to the one implemented in the base class.
     */
    @Override
    public char[] readPassword(String fmt, Object... args) {
        try {
            return (char[])readPassword.invoke(console, fmt, args);
        } catch (Throwable exc) {
            return super.readPassword(fmt, args);
        }
    }
}
