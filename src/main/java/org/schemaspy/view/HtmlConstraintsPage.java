/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
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
package org.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.schemaspy.DbAnalyzer;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.HtmlEncoder;
import org.schemaspy.util.LineWriter;
import org.schemaspy.model.Database;
import org.schemaspy.model.ForeignKeyConstraint;
import org.schemaspy.model.Table;

/**
 * The page that lists all of the constraints in the schema
 *
 * @author John Currier
 */
public class HtmlConstraintsPage extends HtmlFormatter {
    private static HtmlConstraintsPage instance = new HtmlConstraintsPage();
    private int columnCounter;

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlConstraintsPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlConstraintsPage getInstance() {
        return instance;
    }

    public void write(Database database, List<ForeignKeyConstraint> constraints, Collection<Table> tables, File outputDir) throws IOException {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("constraints", constraints);
        scopes.put("tables", tables);

        MustacheWriter mw = new MustacheWriter( outputDir, scopes, getPathToRoot(), database.getName(), false);
        mw.write("layout/constraint.html", "constraints.html", "constraint.js");
    }

    private void writeHeader(Database database, LineWriter html) throws IOException {
        writeHeader(database, null, "Constraints", html);
        html.writeln("<div class='indent'>");
    }

    @Override
    protected void writeFooter(LineWriter html) throws IOException {
        html.writeln("</div>");
        super.writeFooter(html);
    }

    /**
     * Write specified foreign key constraints
     *
     * @param constraints List
     * @param html LineWriter
     * @throws IOException
     */
    private void writeForeignKeyConstraints(List<ForeignKeyConstraint> constraints, LineWriter html) throws IOException {
        Set<ForeignKeyConstraint> constraintsByName = new TreeSet<ForeignKeyConstraint>();
        constraintsByName.addAll(constraints);

        html.writeln("<table width='100%'>");
        html.writeln("<tr><td class='container' valign='bottom'><b>");
        html.write(String.valueOf(constraintsByName.size()));
        html.writeln(" Foreign Key Constraints:</b>");
        html.writeln("</td><td class='container' align='right'>");
        if (sourceForgeLogoEnabled())
            html.writeln("  <a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a>");
        html.writeln("</td></tr>");
        html.writeln("</table><br>");
        html.writeln("<table class='dataTable' border='1' rules='groups'>");
        html.writeln("<colgroup>");
        html.writeln("<colgroup>");
        html.writeln("<colgroup>");
        html.writeln("<colgroup>");
        html.writeln("<thead align='left'>");
        html.writeln("<tr>");
        html.writeln("  <th>Constraint Name</th>");
        html.writeln("  <th>Child Column</th>");
        html.writeln("  <th>Parent Column</th>");
        html.writeln("  <th>Delete Rule</th>");
        html.writeln("</tr>");
        html.writeln("</thead>");
        html.writeln("<tbody>");
        for (ForeignKeyConstraint constraint : constraintsByName) {
            writeForeignKeyConstraint(constraint, html);
        }
        if (constraints.size() == 0) {
            html.writeln(" <tr>");
            html.writeln("  <td class='detail' valign='top' colspan='4'>None detected</td>");
            html.writeln(" </tr>");
        }
        html.writeln("</tbody>");
        html.writeln("</table>");
    }

    /**
     * Write specified foreign key constraint
     *
     * @param constraint ForeignKeyConstraint
     * @param html LineWriter
     * @throws IOException
     */
    private void writeForeignKeyConstraint(ForeignKeyConstraint constraint, LineWriter html) throws IOException {
        boolean even = columnCounter++ % 2 == 0;
        if (even)
            html.writeln("  <tr class='even'>");
        else
            html.writeln("  <tr class='odd'>");
        html.write("  <td class='detail'>");
        html.write(constraint.getName());
        html.writeln("</td>");
        html.write("  <td class='detail'>");
        for (Iterator<TableColumn> iter = constraint.getChildColumns().iterator(); iter.hasNext(); ) {
            TableColumn column = iter.next();
            html.write("<a href='tables/");
            html.write(urlEncode(column.getTable().getName()));
            html.write(".html'>");
            html.write(column.getTable().getName());
            html.write("</a>");
            html.write(".");
            html.write(column.getName());
            if (iter.hasNext())
                html.write("<br>");
        }
        html.writeln("</td>");
        html.write("  <td class='detail'>");
        for (Iterator<TableColumn> iter = constraint.getParentColumns().iterator(); iter.hasNext(); ) {
            TableColumn column = iter.next();
            html.write("<a href='tables/");
            html.write(urlEncode(column.getTable().getName()));
            html.write(".html'>");
            html.write(column.getTable().getName());
            html.write("</a>");
            html.write(".");
            html.write(column.getName());
            if (iter.hasNext())
                html.write("<br>");
        }
        html.writeln("</td>");
        html.write("  <td class='detail'>");
        String ruleText = constraint.getDeleteRuleDescription();
        String ruleName = constraint.getDeleteRuleName();
        html.write("<span title='" + ruleText + "'>" + ruleName + "&nbsp;</span>");
        html.writeln("</td>");
        html.writeln(" </tr>");
    }

    /**
     * Write check constraints associated with the specified tables
     *
     * @param tables Collection
     * @param html LineWriter
     * @throws IOException
     */
    public void writeCheckConstraints(Collection<Table> tables, LineWriter html) throws IOException {
        html.writeln("<a name='checkConstraints'></a><p>");
        html.writeln("<b>Check Constraints:</b>");
        html.writeln("<TABLE class='dataTable' border='1' rules='groups'>");
        html.writeln("<colgroup>");
        html.writeln("<colgroup>");
        html.writeln("<colgroup>");
        html.writeln("<thead align='left'>");
        html.writeln("<tr>");
        html.writeln("  <th>Table</th>");
        html.writeln("  <th>Constraint Name</th>");
        html.writeln("  <th>Constraint</th>");
        html.writeln("</tr>");
        html.writeln("</thead>");
        html.writeln("<tbody>");

        List<Table> tablesByName = DbAnalyzer.sortTablesByName(new ArrayList<Table>(tables));

        int constraintsWritten = 0;

        // iter over all tables...only ones with check constraints will write anything
        for (Table table : tablesByName) {
            constraintsWritten += writeCheckConstraints(table, html);
        }

        if (constraintsWritten == 0) {
            html.writeln(" <tr>");
            html.writeln("  <td class='detail' valign='top' colspan='3'>None detected</td>");
            html.writeln(" </tr>");
        }

        html.writeln("</tbody>");
        html.writeln("</table>");
    }

    /**
     * Write check constraints associated with the specified table (if any)
     *
     * @param table Table
     * @param html LineWriter
     * @throws IOException
     * @return int
     */
    private int writeCheckConstraints(Table table, LineWriter html) throws IOException {
        Map<String, String> constraints = table.getCheckConstraints();  // constraint name -> text pairs
        int constraintsWritten = 0;
        for (String name : constraints.keySet()) {
            html.writeln(" <tr>");
            html.write("  <td class='detail' valign='top'><a href='tables/");
            html.write(urlEncode(table.getName()));
            html.write(".html'>");
            html.write(table.getName());
            html.write("</a></td>");
            html.write("  <td class='detail' valign='top'>");
            html.write(name);
            html.writeln("</td>");
            html.write("  <td class='detail'>");
            html.write(HtmlEncoder.encodeString(constraints.get(name).toString()));
            html.writeln("</td>");
            html.writeln(" </tr>");
            ++constraintsWritten;
        }

        return constraintsWritten;
    }

    @Override
    protected boolean isConstraintsPage() {
        return true;
    }
}