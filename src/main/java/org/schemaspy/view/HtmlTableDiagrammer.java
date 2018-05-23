/*
 * Copyright (C) 2004 - 2010 John Currier
 * Copyright (C) 2017 Daniel Watt
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

import org.schemaspy.model.Table;
import org.schemaspy.util.DiagramUtil;
import org.schemaspy.util.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author John Currier
 * @author Daniel Watt
 */
public class HtmlTableDiagrammer extends HtmlDiagramFormatter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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

        } catch (IOException dotFailure) {
            LOGGER.error("There was an error writing a dot file",dotFailure);
            return false;
        }

        return true;
    }


}
