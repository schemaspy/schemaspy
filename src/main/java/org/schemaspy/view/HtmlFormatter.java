/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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
package org.schemaspy.view;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;

import com.github.mustachejava.util.HtmlEscaper;
import org.schemaspy.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected final boolean encodeComments = Config.getInstance().isEncodeCommentsEnabled();
    private   final boolean isMetered = Config.getInstance().isMeterEnabled();
    protected final boolean displayNumRows = Config.getInstance().isNumRowsEnabled();

    protected HtmlFormatter() {
    }

    /**
     * Override if your output doesn't live in the root directory.
     * If non blank must end with a trailing slash.
     *
     * @return String
     */
    protected String getPathToRoot() {
        return "";
    }

    protected boolean sourceForgeLogoEnabled() {
        return Config.getInstance().isLogoEnabled();
    }


    /**
     * HTML escape the specified string
     *
     * @param string
     * @return
     */
    static String escapeHtml(String string) {
        StringWriter writer = new StringWriter();
        HtmlEscaper.escape(string, writer);
        return writer.toString();
    }

    static String urlEncodeLink(String string) {
        try {
            return URLEncoder.encode(string, Config.DOT_CHARSET).replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            LOGGER.info("Error trying to urlEncode string [{}] with encoding [" + Config.DOT_CHARSET + "]", string);
            return string;
        }
    }
}
