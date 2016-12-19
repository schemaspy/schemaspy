package org.schemaspy.view;

/**
 * Created by rkasa on 2016-12-17.
 */
public class MustacheSchema {
    String name;

    MustacheSchema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
