/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2011 John Currier
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
package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Routine;
import net.sourceforge.schemaspy.model.RoutineParameter;
import net.sourceforge.schemaspy.util.LineWriter;

/**
 * The page that lists all of the routines (stored procedures and functions)
 * in the schema.
 *
 * @author John Currier
 */
public class HtmlRoutinesPage extends HtmlFormatter {
    private static HtmlRoutinesPage instance = new HtmlRoutinesPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlRoutinesPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlRoutinesPage getInstance() {
        return instance;
    }

    public void write(Database db, LineWriter html) throws IOException {
        Collection<Routine> routines = new TreeSet<Routine>(db.getRoutines());

        writeHeader(db, routines, html);

        for (Routine routine : routines) {
            write(routine, html);
        }

        writeFooter(html);
    }

    private void writeHeader(Database db, Collection<Routine> routines, LineWriter html) throws IOException {
        writeHeader(db, null, "Procedures and Functions", html);

        html.writeln("<table width='100%'>");
        html.writeln(" <tr>");
        html.write("  <td class='container'>");
        writeGeneratedOn(db.getConnectTime(), html);
        html.writeln("  </td>");
        if (sourceForgeLogoEnabled())
            html.writeln("  <td class='container' align='right' valign='top' colspan='2'><a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a></td>");
        html.writeln(" </tr>");
        html.writeln(" <tr>");
        html.writeln("  <td class='container'>");

        int numProcs = 0;
        int numFuncs = 0;

        for (Routine routine : routines) {
            String type = routine.getType().toLowerCase();
            if (type.startsWith("proc"))
                ++numProcs;
            else if (type.startsWith("func"))
                ++numFuncs;
        }

        html.write("   <br><b>");
        html.write(db.getName());
        if (db.getSchema() != null) {
            html.write('.');
            html.write(db.getSchema());
        } else if (db.getCatalog() != null) {
            html.write('.');
            html.write(db.getCatalog());
        }
        html.write(" contains " + numProcs + " procedures and " + numFuncs + " functions:");
        html.write("</b><br><div class='indent'>");

        for (Routine routine : routines) {
            html.write("<a href='#" + routine.getName() + "'>" + routine.getName() + "</a>&nbsp;&nbsp;");
        }

        html.writeln("</div>");
        html.writeln("  </td>");
        html.writeln(" </tr>");
        html.writeln(" <tr><td colspan='3'>");
    }

    private void write(Routine routine, LineWriter html) throws IOException {
        html.writeln("  <br><a id='" + routine.getName() + "'></a><hr>");
        html.write("  <br><code><b>" + routine.getType() + " " + routine.getName());
        html.write('(');
        List<RoutineParameter> params = routine.getParameters();
        Iterator<RoutineParameter> iter = params.iterator();
        while (iter.hasNext()) {
            RoutineParameter param = iter.next();
            if (param.getMode() != null) {
                html.write(param.getMode());
                html.write(' ');
            }
            if (param.getName() != null) {
                html.write(param.getName());
                html.write(' ');
            }
            if (param.getType() != null) {
                html.write(param.getType());
            }
            if (iter.hasNext())
                html.write(", ");
        }
        html.write(") ");
        if (routine.getReturnType() != null) {
            html.write("RETURNS ");
            html.writeln(routine.getReturnType());
        }
        html.writeln("</b><br>");
        String indent = "   &nbsp;&nbsp;&nbsp;";
        if (routine.getDefinitionLanguage() != null && routine.getDefinitionLanguage().length() > 0)
            html.writeln(indent + "LANGUAGE " + routine.getDefinitionLanguage() + "<br>");
        if (routine.getType().toLowerCase().startsWith("func")) {
            // applies to return characteristics of functions only
            html.write(indent);
            if (!routine.isDeterministic())
                html.write("NOT ");
            html.writeln("DETERMINISTIC<br>");
        }
        if (routine.getDataAccess() != null && routine.getDataAccess().length() > 0)
            html.writeln(indent + routine.getDataAccess() + "<br>");
        if (routine.getSecurityType() != null && routine.getSecurityType().length() > 0)
            html.writeln(indent + "SQL SECURITY " + routine.getSecurityType() + "<br>");
        if (routine.getComment() != null && routine.getComment().length() > 0)
            html.writeln(indent + "COMMENT '" + routine.getComment() + "'<br>");
        html.writeln("</code><pre>");
        html.writeln(routine.getDefinition());
        html.writeln("</pre>");
    }


    @Override
    protected void writeFooter(LineWriter html) throws IOException {
        html.writeln("</td></tr></table>");
        super.writeFooter(html);
    }

    @Override
    protected boolean isRoutinesPage() {
        return true;
    }
}