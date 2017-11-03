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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.util.HtmlEscaper;
import org.schemaspy.Config;
import org.schemaspy.Revision;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.Dot;
import org.schemaspy.util.HtmlEncoder;
import org.schemaspy.util.LineWriter;

public class HtmlFormatter {
    private static final Logger logger = Logger.getLogger(HtmlFormatter.class.getName());
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
            logger.info("Error trying to urlEncode string [" + string + "] with encoding [" + Config.DOT_CHARSET + "]");
            return string;
        }
    }
}
