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

/**
 * Created by rkasa on 2016-03-26.
 *
 * @author Rafal Kasa
 */
public class MustacheTableDiagram {
    private String name;
    private String fileName;
    private String map;
    private String id;
    private String mapName;
    private String active;
    private boolean isImplied;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active ? "active" : "";
    }

    public boolean isImplied() {
        return isImplied;
    }

    public void setIsImplied(boolean isImplied) {
        this.isImplied = isImplied;
    }
}
