package org.schemaspy.view;

import org.schemaspy.model.Schema;
import org.schemaspy.util.Markdown;

/**
 * Created by rkasa on 2016-12-17.
 */
public class MustacheSchema {
    String name;
    String comment;

    public MustacheSchema(Schema schema,String rootPath) {
        this.name = schema.getName();
        this.comment = Markdown.toHtml(schema.getComment(), rootPath); 
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
