package org.schemaspy.view;

/**
 * Created by rkasa on 2016-03-26.
 */
public class MustacheTableDiagram {
    private String name;
    private String fileName;
    private String map;
    private String id;
    private String mapName;
    private String active;
    private boolean isImplied;
    private boolean isEmbed;

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

    public boolean isEmbed() {
        return isEmbed;
    }

    public void setEmbed(boolean embed) {
        isEmbed = embed;
    }
}
