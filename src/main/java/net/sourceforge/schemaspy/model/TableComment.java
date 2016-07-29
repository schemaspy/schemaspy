package net.sourceforge.schemaspy.model;

/**
 * Created by rkasa on 2016-06-28.
 */
public class TableComment {
    private String component;
    private String type;
    private String doc;

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }
}
