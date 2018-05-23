/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
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
package org.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The page that contains links to the various schemas that were analyzed
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlMultipleSchemasIndexPage extends HtmlFormatter {
    private static HtmlMultipleSchemasIndexPage instance = new HtmlMultipleSchemasIndexPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlMultipleSchemasIndexPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlMultipleSchemasIndexPage getInstance() {
        return instance;
    }


    public void write(File outputDir, String dbName,MustacheCatalog catalog, List<MustacheSchema> schemas, DatabaseMetaData meta) throws IOException {

        String connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("databaseName", dbName);
        scopes.put("connectTime", connectTime);

        scopes.put("databaseProduct", getDatabaseProduct(meta));
        scopes.put("schemas", schemas);
        scopes.put("catalog", catalog);
        scopes.put("schemasNumber", Integer.toString(schemas.size()));

        scopes.put("multipleSchemas", true);



        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", dbName, true);
        mw.write("multi.html", "index.html", "");
    }

//    private void writeHeader(String databaseName, DatabaseMetaData meta4, int numberOfSchemas, boolean showIds9, String aSchema, LineWriter html) throws IOException {
//        String connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());
//
//        html.writeln("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>");
//        html.writeln("<html>");
//        html.writeln("<head>");
//        html.write("  <title>SchemaSpy Analysis");
//        if (databaseName != null) {
//            html.write(" of Database ");
//            html.write(databaseName);
//        }
//        html.writeln("</title>");
//        html.write("  <link rel=stylesheet href='");
//        html.write(aSchema);
//        html.writeln("/schemaSpy.css' type='text/css'>");
//        html.writeln("  <meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=" + Config.getInstance().getCharset() + "'>");
//        html.writeln("</head>");
//        html.writeln("<body>");
//        writeTableOfContents(html);
//        html.writeln("<div class='content' style='clear:both;'>");
//        html.writeln("<table width='100%' border='0' cellpadding='0'>");
//        html.writeln(" <tr>");
//        html.write("  <td class='heading' valign='top'><h1>");
//        html.write("SchemaSpy Analysis");
//        if (databaseName != null) {
//            html.write(" of Database ");
//            html.write(databaseName);
//        }
//        html.writeln("</h1></td>");
//        html.writeln(" </tr>");
//        html.writeln("</table>");
//        html.writeln("<table width='100%'>");
//        html.writeln(" <tr><td class='container'>");
//        writeGeneratedOn(connectTime, html);
//        html.writeln(" </td></tr>");
//        html.writeln(" <tr>");
//        html.write("  <td class='container'>");
//        if (meta != null) {
//            html.write("Database Type: ");
//            html.write(getDatabaseProduct(meta));
//        }
//        html.writeln("  </td>");
//        html.writeln("  <td class='container' align='right' valign='top' rowspan='3'>");
//        html.write("    <br>");
//        html.writeln("  </td>");
//        html.writeln(" </tr>");
//        html.writeln("</table>");
//
//        html.writeln("<div class='indent'>");
//        html.write("<b>");
//        html.write(String.valueOf(numberOfSchemas));
//        if (databaseName != null)
//            html.write(" Schema");
//        else
//            html.write(" Database");
//        html.write(numberOfSchemas == 1 ? "" : "s");
//        html.writeln(":</b>");
//        html.writeln("<TABLE class='dataTable' border='1' rules='groups'>");
//        html.writeln("<colgroup>");
//        html.writeln("<thead align='left'>");
//        html.writeln("<tr>");
//        html.write("  <th valign='bottom'>");
//        if (databaseName != null)
//            html.write("Schema");
//        else
//            html.write("Database");
//        html.writeln("</th>");
//        if (showIds)
//            html.writeln("  <th align='center' valign='bottom'>ID</th>");
//        html.writeln("</tr>");
//        html.writeln("</thead>");
//        html.writeln("<tbody>");
//    }

    /**
     * Copy / paste from Database, but we can't use Database here...
     *
     * @param meta DatabaseMetaData
     * @return String
     */
    private String getDatabaseProduct(DatabaseMetaData meta) {
        try {
            return meta.getDatabaseProductName() + " - " + meta.getDatabaseProductVersion();
        } catch (SQLException exc) {
            return "";
        }
    }
}
