/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.output.diagram.vizjs;

import org.apache.commons.io.IOUtils;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.diagram.DiagramProducer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

public class VizJSDot implements DiagramProducer {

    private static final String ICON_SIZE = " , width: \"261px\" , height: \"261px\"";
    private static final int MB_64 = 1024 * 1024 * 64;
    protected ScriptEngine scriptEngine;

    public VizJSDot() {
        try {
            InputStream vizJs = VizJSDot.class.getResourceAsStream("/viz.js");
            if (vizJs == null) {
                throw new IllegalArgumentException("viz.js not found");
            }
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
            scriptEngine.eval(IOUtils.toString(vizJs,"UTF-8"));
        } catch (Exception e) {
            throw new IllegalArgumentException("viz.js", e);
        }
    }

    @Override
    public String getImplementationDetails() {
        return "Viz.js 1.7.1 (Graphviz 2.40.1, Expat 2.1.0, Emscripten 1.37.9)";
    }

    @Override
    public String getDiagramFormat() {
        return "svg";
    }

    public String generateDiagram(File dotFile, File diagramFile) {
        try {
            String dotSource = IOUtils.toString(dotFile.toURI().toURL(), "UTF-8");
            String svg = toSvg(dotSource, MB_64);
            try (FileWriter diagramWriter = new FileWriter(diagramFile)){
                IOUtils.write(svg, diagramWriter);
            }
            return "";
        } catch (Exception e) {
            throw new DiagramException(e.getMessage());
        }
    }

    protected String toSvg(final String dotSource, int jsEngineMemorySize) {
        String fixed = fixPathsAndUrls(dotSource);
        try {
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("dotSource", fixed);
            return (String) scriptEngine.eval("Viz(dotSource,options = { totalMemory: "+jsEngineMemorySize
                    +" , images: [" +
                    "{ path: \"../../images/foreignKeys.png\"" + ICON_SIZE + " }," +
                    "{ path: \"../../images/primaryKeys.png\"" + ICON_SIZE + " }]})");
        } catch (ScriptException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String fixPathsAndUrls(final String dotSource) {
        String fixedImage = dotSource.replaceAll("(<IMG SRC=\\\").*(images.*)(\\\"/>)","$1../../$2$3");
        String addedTableIfMissing = fixedImage.replaceAll("(URL=\")([^tables])(.*)(\")","$1tables/$2$3$4");
        String fixedUrlsInOrphansRelationShips = addedTableIfMissing.replaceAll("(URL=\")(.*)(\")","$1../../$2$3");
        return fixedUrlsInOrphansRelationShips;
    }

}
