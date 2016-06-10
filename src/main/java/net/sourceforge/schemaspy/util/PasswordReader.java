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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

/**
 * This class prompts the user for a password and attempts to mask input with
 * "*"
 */
public class PasswordReader {
    private static PasswordReader instance;

    public static synchronized PasswordReader getInstance() {
        if (instance == null) {
            try {
                instance = new ConsolePasswordReader();
            } catch (Throwable exc) {
                // Java6+ version can't be loaded, so revert to this implementation
                instance = new PasswordReader();
            }
        }

        return instance;
    }

    /**
     * Use {@link #getInstance()} instead.
     */
    protected PasswordReader() {
    }

    /**
     * Matches the contract of Java 1.6+'s {@link java.io.Console#readPassword}
     * except that our own IOError is thrown in place of the 1.6-specific IOError.
     * By matching the contract we can use this implementation when
     * running in a 1.5 JVM or the much better implementation that
     * was introduced in 1.6 when running in a JVM that supplies it.
     *
     * @param fmt
     * @param args
     * @return
     */
    public char[] readPassword(String fmt, Object ... args) {
        InputStream in = System.in;
        char[] lineBuffer;
        char[] buf = lineBuffer = new char[128];
        int room = buf.length;
        int offset = 0;
        int ch;
        boolean reading = true;

        Masker masker = new Masker(String.format(fmt, args));
        masker.start();

        try {
            while (reading) {
                switch (ch = in.read()) {
                    case -1:
                    case '\n':
                        reading = false;
                        break;

                    case '\r':
                        int c2 = in.read();
                        if (c2 != '\n' && c2 != -1) {
                            if (!(in instanceof PushbackInputStream)) {
                                in = new PushbackInputStream(in);
                            }
                            ((PushbackInputStream)in).unread(c2);
                        } else {
                            reading = false;
                        }
                        break;

                    default:
                        if (--room < 0) {
                            buf = new char[offset + 128];
                            room = buf.length - offset - 1;
                            System.arraycopy(lineBuffer, 0, buf, 0, offset);
                            Arrays.fill(lineBuffer, ' ');
                            lineBuffer = buf;
                        }
                        buf[offset++] = (char)ch;
                        break;
                }
            }
        } catch (IOException exc) {
            throw new IOError(exc);
        } finally {
            masker.stopMasking();
        }

        if (offset == 0) {
            return null;
        }
        char[] password = new char[offset];
        System.arraycopy(buf, 0, password, 0, offset);
        Arrays.fill(buf, ' ');
        return password;
    }

    /**
     * Simple thread that constantly overwrites (masking) whatever
     * the user is typing as their password.
     */
    private static class Masker extends Thread {
        private volatile boolean masking = true;
        private final String mask;

        /**
         *@param prompt The prompt displayed to the user
         */
        public Masker(String prompt) {
            // mask that will be printed every iteration
            // it includes spaces to replace what's typed
            // and backspaces to move back over them
            mask = "\r" + prompt + "     \010\010\010\010\010";

            // set our priority to something higher than the caller's
            setPriority(Thread.currentThread().getPriority() + 1);
        }

        /**
         * Keep masking until asked to stop
         */
        @Override
        public void run() {
            while (masking) {
                System.out.print(mask);
                try {
                    sleep(100);
                } catch (InterruptedException iex) {
                    interrupt();
                    masking = false;
                }
            }
        }

        /**
         * Stop masking the password
         */
        public void stopMasking() {
            masking = false;
        }
    }

    /**
     * Our own implementation of the Java 1.6 IOError class.
     */
    public class IOError extends Error {
        private static final long serialVersionUID = 20100629L;

        public IOError(Throwable cause) {
            super(cause);
        }
    }
}
