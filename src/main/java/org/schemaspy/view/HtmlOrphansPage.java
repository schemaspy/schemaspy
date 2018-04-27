/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Wojciech Kasa
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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.util.Dot;
import org.schemaspy.util.LineWriter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The page that contains the all tables that aren't related to others (orphans)
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Wojciech Kasa
 * @author Daniel Watt
 */
public class HtmlOrphansPage extends HtmlDiagramFormatter {
    private static HtmlOrphansPage instance = new HtmlOrphansPage();
    private static int MAX_COLUMNS = 4;

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlOrphansPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlOrphansPage getInstance() {
        return instance;
    }

    public boolean write(Database db, List<Table> orphanTables, File diagramDir, File outputDir) throws IOException {
        Dot dot = getDot();
        if (dot == null)
            return false;

        Set<Table> orphansWithImpliedRelationships = new HashSet<Table>();

        Collections.sort(orphanTables, new Comparator() {
            @Override
            public int compare(Object t1, Object t2) {
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
            }
        });

        for (Table table : orphanTables) {
            if (!table.isOrphan(true)){
                orphansWithImpliedRelationships.add(table);
            }
        }

        try {
            StringBuilder maps = new StringBuilder(64 * 1024);
            int element = 0;
            List<MustacheTable> mustacheTables = new ArrayList<>();
            for (Table table : orphanTables) {
                element++;
                String dotBaseFilespec = table.getName();

                File dotFile = new File(diagramDir, dotBaseFilespec + ".1degree.dot");
                File imgFile = new File(diagramDir, dotBaseFilespec + ".1degree." + dot.getFormat());

                try (LineWriter dotOut = new LineWriter(dotFile, Config.DOT_CHARSET)) {
                    DotFormatter.getInstance().writeOrphan(table, dotOut, outputDir);
                } catch (IOException e) {
                    throw new IOException(e);
                }

                try {
                    maps.append(dot.generateDiagram(dotFile, imgFile));
                } catch (Dot.DotFailure dotFailure) {
                    System.err.println(dotFailure);
                    return false;
                }
                mustacheTables.add(new MustacheTable(table, imgFile.getName()));
            }

            HashMap<String, Object> scopes = new HashMap<String, Object>();
            scopes.put("mustacheTables", mustacheTables);
            int size = 12/MAX_COLUMNS;
            scopes.put("size", size);
            scopes.put("maps", maps);

            MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
            mw.write("orphans.html", "orphans.html", "");

            return true;
        } finally {
        }
    }
}
