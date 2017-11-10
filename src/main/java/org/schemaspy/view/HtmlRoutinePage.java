package org.schemaspy.view;

import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;

import java.io.File;
import java.util.HashMap;

public class HtmlRoutinePage extends HtmlFormatter {
    private static HtmlRoutinePage instance = new HtmlRoutinePage();

    public static HtmlRoutinePage getInstance() {
        return instance;
    }

    public void write(Database db, Routine routine, File outputDir) {
        HashMap<String, Object> scopes = new HashMap<String, Object>();
        scopes.put("routine", routine);
        scopes.put("parameters",routine.getParameters());
        scopes.put("definitionExists",routine.getDefinition() != null);

        MustacheWriter mw = new MustacheWriter(outputDir, scopes, getPathToRoot(), db.getName(), false);
        mw.write("routines/routine.html", "routines/" + routine.getName() + ".html", "routine.js");
    }

    @Override protected String getPathToRoot() {
        return "../";
    }
}
