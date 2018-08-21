/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016, 2017 Ismail Simsek
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The page that contains links to the various schemas that were analyzed
 *
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 */
public class HtmlMultipleSchemasIndexPage extends HtmlFormatter {
    private static HtmlMultipleSchemasIndexPage instance = new HtmlMultipleSchemasIndexPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlMultipleSchemasIndexPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlMultipleSchemasIndexPage getInstance() {
        return instance;
    }


    public void write(File outputDir, String dbName, MustacheCatalog catalog, List<MustacheSchema> schemas, String description, String productName) throws IOException {

        String connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("databaseName", dbName);
        scopes.put("description", description);
        scopes.put("connectTime", connectTime);

        scopes.put("databaseProduct", productName);
        scopes.put("schemas", schemas);
        scopes.put("catalog", catalog);
        scopes.put("schemasNumber", Integer.toString(schemas.size()));

        scopes.put("multipleSchemas", true);



        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", dbName, true);
        mw.write("multi.html", "index.html", "");
    }
}
