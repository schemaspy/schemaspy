package org.schemaspy.view;

import org.schemaspy.model.Catalog;
import org.schemaspy.util.Markdown;

/**
 * Created by rkasa on 2016-12-17.
 */
public class MustacheCatalog {
    String name;
    String comment;

    public MustacheCatalog(Catalog catalog,String rootPath) {
        this.name = catalog.getName();
        this.comment = Markdown.toHtml(catalog.getComment(), rootPath); 
    }
	
    public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
