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

import org.schemaspy.output.diagram.DiagramResult;

/**
 * Created by rkasa on 2016-03-26.
 *
 * @author Rafal Kasa
 */
public class MustacheTableDiagram {
    private final String name;
    private final DiagramResult diagram;
    private final boolean isImplied;
    private String active;

    public MustacheTableDiagram(final String diagramName, final DiagramResult diagram, final boolean isImplied) {
        this.name = diagramName;
        this.diagram = diagram;
        this.isImplied = isImplied;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return diagram.getFileName();
    }

    public String getMap() {
        return diagram.getMap();
    }

    public String getId() {
        return name.replaceAll("\\s", "").toLowerCase() + "DegreeImg";
    }

    public String getMapName() {
        return diagram.getMapName();
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

    public boolean isEmbed() {
        return diagram.isEmbed();
    }
}
