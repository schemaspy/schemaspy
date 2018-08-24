/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.view;

import org.schemaspy.model.Table;
import org.schemaspy.util.Dot;
import org.schemaspy.util.Writers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * The page that contains the all tables that aren't related to others (orphans)
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 * @author Nils Petzaell
 */
public class HtmlOrphansPage extends HtmlDiagramFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int KB_64 = 64 * 1024;

    private final MustacheCompiler mustacheCompiler;

    public HtmlOrphansPage(MustacheCompiler mustacheCompiler) {
        this.mustacheCompiler = mustacheCompiler;
    }

    public boolean write(
            List<Table> orphanTables,
            File diagramDir,
            String outputDir,
            Writer writer
    ) throws IOException {
        Dot dot = getDot();
        if (dot == null)
            return false;


        Collections.sort(orphanTables, (Comparator) (t1, t2) -> {
            Integer size1 = ((Table) t1).getColumns().size();
            Integer size2 = ((Table) t2).getColumns().size();
            int sizeComp = size1.compareTo(size2);

            if (sizeComp != 0) {
                return sizeComp;
            } else {
                String name1 = ((Table) t1).getName();
                String name2 = ((Table) t1).getName();
                return name1.compareTo(name2);
            }
        });

        Set<Table> orphansWithImpliedRelationships = new HashSet<>();
        for (Table table : orphanTables) {
            if (!table.isOrphan(true)){
                orphansWithImpliedRelationships.add(table);
            }
        }

        StringBuilder maps = new StringBuilder(KB_64);
        List<MustacheTable> mustacheTables = new ArrayList<>();
        for (Table table : orphanTables) {
            String dotBaseFilespec = table.getName();

            File dotFile = new File(diagramDir, dotBaseFilespec + ".1degree.dot");

            try (PrintWriter dotOut = Writers.newPrintWriter(dotFile)) {
                DotFormatter.getInstance().writeOrphan(table, dotOut, outputDir);
            } catch (IOException e) {
                throw new IOException(e);
            }

            File imgFile = new File(diagramDir, dotBaseFilespec + ".1degree." + dot.getFormat());
            try {
                maps.append(dot.generateDiagram(dotFile, imgFile));
            } catch (Dot.DotFailure dotFailure) {
                LOGGER.error("Failed to write Orphan '{}'", table.getName(), dotFailure);
                return false;
            }
            mustacheTables.add(new MustacheTable(table, imgFile.getName()));
        }

        PageData pageData = new PageData.Builder()
                .templateName("orphans.html")
                .scriptName("")
                .addToScope("mustacheTables", mustacheTables)
                .addToScope("maps", maps)
                .depth(0)
                .getPageData();

        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write orphans page", e);
        }
        return true;
    }
}
