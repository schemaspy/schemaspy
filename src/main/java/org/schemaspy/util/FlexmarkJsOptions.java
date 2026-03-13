/*
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
package org.schemaspy.util;

import com.vladsch.flexmark.util.data.DataHolder;

import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Create flexmark options using java script
 *
 * @author Anselm Kruis
 */
public class FlexmarkJsOptions {
    public static DataHolder getOptions() {
        String jsFile = System.getProperty("schemaspy.flexmark.options.js");
        if (null == jsFile) {
            throw new RuntimeException("system property \"schemaspy.flexmark.options.js\" not set");
        }
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval(new FileReader(jsFile));
            return (DataHolder) (((Invocable) engine).invokeFunction("getOptions"));
        } catch (Exception exc) {
            throw new IllegalArgumentException("Failure to execute js function getOptions()", exc);
        }
    }
}
