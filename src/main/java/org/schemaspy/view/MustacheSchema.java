/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Ismail Simsek
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

import org.schemaspy.model.Schema;
import org.schemaspy.util.Markdown;

/**
 * Created by rkasa on 2016-12-17.
 *
 * @author Rafal Kasa
 * @author Ismail Simsek
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
