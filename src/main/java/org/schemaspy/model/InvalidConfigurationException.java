/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.model;

/**
 * Base class to indicate that there was problem with how SchemaSpy was configured / used.
 *
 * @author John Currier
 * @author Nils Petzaell
 */
public class InvalidConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String paramName;
    private final String paramValue;

    /**
     * When a message is sufficient
     *
     * @param msg
     */
    public InvalidConfigurationException(String msg) {
        super(msg);
        paramName = null;
        paramValue = null;
    }

    /**
     * When there's an associated root cause.
     * The resultant msg will be a combination of <code>msg</code> and cause's <code>msg</code>.
     *
     * @param msg
     * @param cause
     */
    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
        paramName = null;
        paramValue = null;
    }

    public InvalidConfigurationException(String msg, Throwable cause, String paramName, String paramValue) {
        super(msg, cause);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    /**
     * When there are no details other than the root cause
     *
     * @param cause
     */
    public InvalidConfigurationException(Throwable cause) {
        super(cause);
        paramName = null;
        paramValue = null;
    }

    public InvalidConfigurationException(Throwable cause, String paramName, String paramValue) {
        super(cause);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }


    public String getParamValue() {
        return paramValue;
    }
}
