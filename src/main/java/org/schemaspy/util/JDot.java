package org.schemaspy.util;

import org.apache.commons.io.IOUtils;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

public class JDot {

    private static final String SIMULATE_VERSION = "2.26";
    private static final String ICON_SIZE = " , width: \"261px\" , height: \"261px\"";
    private static final int ONE_GB = 1 << 30;
    protected ScriptEngine scriptEngine;
    protected String outputDirectoryName;

    public JDot(String outputDirectoryName) {
        this.outputDirectoryName=outputDirectoryName;
        try {
            InputStream vizJs = JDot.class.getResourceAsStream("/viz.js");
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

    protected String toSvg(String dotSource, int jsEngineMemorySize) {
        try {
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("dotSource", dotSource);
            return (String) scriptEngine.eval("Viz(dotSource,options = { totalMemory: "+jsEngineMemorySize
                    +" , images: [" +
                    "{ path: \""+outputDirectoryName+"/images/foreignKeys.png\"" + ICON_SIZE + " }," +
                    "{ path: \""+outputDirectoryName+"/images/primaryKeys.png\"" + ICON_SIZE + " }]})");
        } catch (ScriptException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getVersion(){
        return SIMULATE_VERSION;
    }

    public String renderDotByJvm(File dotFile, File diagramFile) throws DotFailure {
        try {
            String dotSource = IOUtils.toString(dotFile.toURI().toURL(), "UTF-8");
            String svg = toSvg(dotSource, ONE_GB);
            try (FileWriter diagramWriter = new FileWriter(diagramFile)){
                IOUtils.write(svg, diagramWriter);
            }
            return "";
        } catch (Exception e) {
            throw new DotFailure(e.getMessage());
        }
    }
}
