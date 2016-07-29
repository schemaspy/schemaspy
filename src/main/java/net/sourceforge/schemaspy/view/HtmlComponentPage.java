/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
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
package net.sourceforge.schemaspy.view;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableComment;
import net.sourceforge.schemaspy.util.Markdown;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ho.yaml.YamlDecoder;

/**
 * The main index that contains all tables and views that were evaluated
 *
 * @author John Currier
 */
public class HtmlComponentPage extends HtmlFormatter {
    private static HtmlComponentPage instance = new HtmlComponentPage();

    /**
     * Singleton: Don't allow instantiation
     */
    private HtmlComponentPage() {
    }

    /**
     * Singleton accessor
     *
     * @return the singleton instance
     */
    public static HtmlComponentPage getInstance() {
        return instance;
    }

    public void write(Database database, Collection<Table> tables, File outputDir) throws IOException {
        List<MustacheTable> mustacheTables = new ArrayList<>();

        for(Table table: tables) {
            String comment = table.getComments();
            if (comment != null) {
//                InputStream inputStream = IOUtils.toInputStream(comment, "UTF-8");
//                YamlDecoder dec = new YamlDecoder(inputStream);
//                TableComment tableComment = dec.readObjectOfType(TableComment.class);
//                String comments = Markdown.toHtml(tableComment.getDoc(),"");
            }
        }

        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("tables", mustacheTables);
        scopes.put("database", database);

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, "", database.getName());
        mw.write("/layout/components.html", "components.html", "components.js");
    }


}
