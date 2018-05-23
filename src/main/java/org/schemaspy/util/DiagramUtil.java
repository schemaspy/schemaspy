/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.util;

import org.schemaspy.view.MustacheTableDiagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by rkasa on 2016-04-16.
 *
 * @author Rafal Kasa
 * @author Daniel Watt
 */
public class DiagramUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void generateDiagram(String diagramName, Dot dot, File dotFile, File diagramFile, List<MustacheTableDiagram> diagrams, boolean isActive, boolean isImplied) throws IOException {
        if (dotFile.exists()) {
            String mapDegreesDotFile = dot.generateDiagram(dotFile, diagramFile);
            createDiagram(diagramName, diagramFile, mapDegreesDotFile, diagrams, isActive, isImplied);
        } else {
            Files.deleteIfExists(dotFile.toPath());
            Files.deleteIfExists(diagramFile.toPath());

        }
    }

    private static void createDiagram(String diagramName, File diagramFile, String diagramMap, List<MustacheTableDiagram> diagrams, boolean isActive, boolean isImplied) {
        MustacheTableDiagram diagram = new MustacheTableDiagram();
        diagram.setActive(isActive);
        diagram.setName(diagramName);
        diagram.setFileName(diagramFile.getName());
        diagram.setMap(diagramMap);
        String diagramId = diagramName.replace(" ", "").toLowerCase();
        diagram.setId(diagramId + "DegreeImg");
        diagram.setMapName(diagramMapName(diagramMap));
        diagram.setIsImplied(isImplied);
        diagrams.add(diagram);
    }

    private static String diagramMapName(String diagramMap) {
        BufferedReader reader = new BufferedReader(new StringReader(diagramMap));
        String line;
        String diagramMapName="";
        try {
            line = reader.readLine();
            if (line != null) {
                diagramMapName = line.substring(9,line.indexOf("name")-2);
            }
        } catch (IOException e) {
            LOGGER.error("Error reading diagram map",e);
        }
        return diagramMapName;
    }

    public static Object diagramExists(List<MustacheTableDiagram> diagrams) {
        Object exists = null;
        if  (!diagrams.isEmpty()) {
            exists = new Object();
        }
        return exists;
    }

    public static void markFirstAsActive(List<MustacheTableDiagram> diagrams) {
        if (diagrams != null && !diagrams.isEmpty()) {
            MustacheTableDiagram diagram = diagrams.get(0);
            if (diagram != null) {
                diagram.setActive(true);
            }
        }
    }
}
