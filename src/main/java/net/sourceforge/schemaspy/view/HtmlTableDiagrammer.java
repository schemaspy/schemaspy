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
package net.sourceforge.schemaspy.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.DiagramUtil;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlTableDiagrammer extends HtmlDiagramFormatter {
    private static HtmlTableDiagrammer instance = new HtmlTableDiagrammer();

    private HtmlTableDiagrammer() {
    }

    public static HtmlTableDiagrammer getInstance() {
        return instance;
    }

    public boolean write(Table table, File diagramDir, List<MustacheTableDiagram> diagrams) {
        try {
            Dot dot = getDot();
            if (dot == null) return false;

            File oneDegreeDotFile = new File(diagramDir, table.getName() + ".1degree.dot");
            File oneDegreeDiagramFile = new File(diagramDir, table.getName() + ".1degree." + dot.getFormat());
            File twoDegreesDotFile = new File(diagramDir, table.getName() + ".2degrees.dot");
            File twoDegreesDiagramFile = new File(diagramDir, table.getName() + ".2degrees." + dot.getFormat());
            File oneImpliedDotFile = new File(diagramDir, table.getName() + ".implied1degrees.dot");
            File oneImpliedDiagramFile = new File(diagramDir, table.getName() + ".implied1degrees." + dot.getFormat());
            File twoImpliedDotFile = new File(diagramDir, table.getName() + ".implied2degrees.dot");
            File twoImpliedDiagramFile = new File(diagramDir, table.getName() + ".implied2degrees." + dot.getFormat());


            DiagramUtil.generateDiagram("One", dot, oneDegreeDotFile, oneDegreeDiagramFile, diagrams, true, false);
            DiagramUtil.generateDiagram("Two degrees", dot, twoDegreesDotFile, twoDegreesDiagramFile, diagrams, false, false);
            DiagramUtil.generateDiagram("One implied", dot, oneImpliedDotFile, oneImpliedDiagramFile, diagrams, false, true);
            DiagramUtil.generateDiagram("Two implied", dot, twoImpliedDotFile, twoImpliedDiagramFile, diagrams, false, true);

        } catch (Dot.DotFailure dotFailure) {
            System.err.println(dotFailure);
            return false;
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
            return false;
        }

        return true;
    }


}
