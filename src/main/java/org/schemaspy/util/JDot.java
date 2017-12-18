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
    private static final String FK = "[FK] ";
    private static final String PK = "[PK] ";
    protected ScriptEngine scriptEngine;

    public JDot() {
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

    protected String toSvg(String dotSource, int memSize) {
        try {
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("dotSource", dotSource);
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("memSize", memSize);
            return (String) scriptEngine.eval("Viz(dotSource,{ totalMemory: memSize })");
        } catch (ScriptException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getVersion(){
        return SIMULATE_VERSION;
    }

    public String renderDotByJvm(File dotFile, File diagramFile) throws Dot.DotFailure {
        try {
            String dotSource = IOUtils.toString(dotFile.toURI().toURL(), "UTF-8")
                    .replaceAll("<IMG SRC=\".*/images/foreignKeys.png\"/>", FK)
                    .replaceAll("<IMG SRC=\".*/images/primaryKeys.png\"/>", PK);
            String svg = toSvg(dotSource, 1 << 30);
            try (FileWriter diagramWriter = new FileWriter(diagramFile)){
                IOUtils.write(svg, diagramWriter);
            }
            return "";
        } catch (Exception e) {
            throw new Dot.DotFailure(e.getMessage());
        }
    }
}
