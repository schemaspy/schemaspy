package net.sourceforge.schemaspy.model;

import java.util.*;

/**
 * Created by rkasa on 2016-04-15.
 */
public class DatabaseObject {
    private String name;
    private final String orginalName;
    private final String typeName;
    private final Integer type;
    private final int length;
    private final Set<TableColumn> parents;
    private final Set<TableColumn> children;

    public DatabaseObject(TableColumn object) {
        this.name = object.getName();
        this.orginalName = new String(object.getName());
        this.typeName = object.getTypeName();
        this.type = object.getType();
        this.length = object.getLength();
        this.parents = object.getParents();
        this.children = object.getChildren();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public Set<TableColumn> getParents() {
        return parents;
    }

    public Set<TableColumn> getChildren() {
        return children;
    }

    public String getOrginalName() {
        return orginalName;
    }

    @Override
    public String toString() {
        return name;
    }
}
