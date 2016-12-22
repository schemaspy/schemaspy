package org.schemaspy.model;

import java.util.*;

/**
 * Created by rkasa on 2016-04-15.
 */
public class DatabaseObject implements Comparable<DatabaseObject>{
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
    
	@Override
	public int compareTo(DatabaseObject column2) {
        int rc = this.getName().compareToIgnoreCase(column2.getName());
        if (rc == 0) {
            if (this.getType() != null && column2.getType() != null)
            	// type is exact while typeName can be adorned with additional stuff (e.g. MSSQL appends " identity" for auto-inc keys)
            	rc = this.getType().compareTo(column2.getType());
            else
                rc = this.getTypeName().compareToIgnoreCase(column2.getTypeName());
        }
        if (rc == 0)
            rc = this.getLength() - column2.getLength();
        return rc;
	}

}
