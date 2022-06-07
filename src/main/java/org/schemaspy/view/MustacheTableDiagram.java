/*
 * Copyright (C) 2016 Rafal Kasa
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.view;

import org.schemaspy.output.diagram.DiagramResults;

/**
 * Created by rkasa on 2016-03-26.
 *
 * @author Rafal Kasa
 */
public class MustacheTableDiagram {
    private String name;
    private DiagramResults diagram;
    private String active;
    private boolean isImplied;

    public MustacheTableDiagram() { }

    public MustacheTableDiagram(final String diagramName, final DiagramResults diagram) {
        this.name = diagramName;
        this.diagram = diagram;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return diagram.getDiagramFile().getName();
    }

    public String getMap() {
        return diagram.getDiagramMap();
    }

    public String getId() {
        return name.replaceAll("\\s", "").toLowerCase() + "DegreeImg";
    }

    public String getMapName() {
        return diagram.getDiagramMapName();
    }

    public String getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active ? "active" : null;
    }

    public boolean isImplied() {
        return isImplied;
    }

    public void setIsImplied(boolean isImplied) {
        this.isImplied = isImplied;
    }

    public boolean isEmbed() {
        return diagram.getImageFormat().equalsIgnoreCase("svg");
    }
}
