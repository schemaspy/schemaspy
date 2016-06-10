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
package net.sourceforge.schemaspy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.sourceforge.schemaspy.model.ProcessExecutionException;
import net.sourceforge.schemaspy.util.LineWriter;
import net.sourceforge.schemaspy.view.HtmlMultipleSchemasIndexPage;

/**
 * @author John Currier
 */
public final class MultipleSchemaAnalyzer {
    private static MultipleSchemaAnalyzer instance = new MultipleSchemaAnalyzer();
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final boolean fineEnabled = logger.isLoggable(Level.FINE);

    private MultipleSchemaAnalyzer() {
    }

    public static MultipleSchemaAnalyzer getInstance() {
        return instance;
    }

    public void analyze(String dbName, DatabaseMetaData meta, String schemaSpec, List<String> schemas,
            List<String> args, Config config) throws SQLException, IOException {
        long start = System.currentTimeMillis();
        String loadedFrom = Config.getLoadedFromJar();
        File outputDir = config.getOutputDir();

        List<String> genericCommand = new ArrayList<String>();
        genericCommand.add("java");
        genericCommand.add("-Doneofmultipleschemas=true");
        if (new File(loadedFrom).isDirectory()) {
            genericCommand.add("-cp");
            genericCommand.add(loadedFrom);
            genericCommand.add(Main.class.getName());
        } else {
            genericCommand.add("-jar");
            genericCommand.add(loadedFrom);
        }

        args = new ArrayList<String>(args); // rude to modify caller's params, so make a copy

        args.remove("-all");
        SchemaAnalyzer.yankParam(args, "-o");
        SchemaAnalyzer.yankParam(args, "-s");

        // these are passed through environment variables
        SchemaAnalyzer.yankParam(args, "-p");
        SchemaAnalyzer.yankParam(args, "-i");
        SchemaAnalyzer.yankParam(args, "-I");
        SchemaAnalyzer.yankParam(args, "-x");
        SchemaAnalyzer.yankParam(args, "-X");

        for (String next : args) {
            if (next.startsWith("-"))
                genericCommand.add(next);
            else
                genericCommand.add("\"" + next + "\"");
        }

        List<String> populatedSchemas;
        if (schemas == null) {
            System.out.println("Analyzing schemas that match regular expression '" + schemaSpec + "':");
            System.out.println("(use -schemaSpec on command line or in .properties to exclude other schemas)");
            populatedSchemas = getPopulatedSchemas(meta, schemaSpec, false);
            if (populatedSchemas.isEmpty())
                populatedSchemas = getPopulatedSchemas(meta, schemaSpec, true);
            if (populatedSchemas.isEmpty())
                populatedSchemas = Arrays.asList(new String[] {config.getUser()});
        } else {
            System.out.println("Analyzing schemas:");
            populatedSchemas = schemas;
        }

        for (String populatedSchema : populatedSchemas)
            System.out.print(" " + populatedSchema);
        System.out.println();

        writeIndexPage(dbName, populatedSchemas, meta, outputDir, config.getCharset());

        Map<String, String> env = System.getenv();
        List<String> childEnv = new ArrayList<String>();
        for (Entry<String, String> entry : env.entrySet()) {
            childEnv.add(entry.getKey() + '=' + entry.getValue());
        }

        // safer to pass password in environment so it can't be directly seen in cmd line
        childEnv.add("schemaspy.pw=" + config.getPassword());

        // some shells expand these regular expressions, so attempt to preserve them
        // by passing in the environment
        childEnv.add("schemaspy.tableInclusions=" + config.getTableInclusions());
        childEnv.add("schemaspy.tableExclusions=" + config.getTableExclusions());
        childEnv.add("schemaspy.columnExclusions=" + config.getColumnExclusions());
        childEnv.add("schemaspy.indirectColumnExclusions=" + config.getIndirectColumnExclusions());

        for (String schema : populatedSchemas) {
            List<String> command = new ArrayList<String>(genericCommand);
            // if no database was specified then we're dealing with a database
            // that treats a schema as the database
            if (dbName == null)
                command.add("-db");
            else
                command.add("-s");
            command.add(schema);
            command.add("-o");
            command.add(new File(outputDir, schema).toString());
            System.out.println("Analyzing " + schema);
            System.out.flush();
            logger.fine("Analyzing schema with: " + command);
            Process java = Runtime.getRuntime().exec(command.toArray(new String[]{}), childEnv.toArray(new String[]{}));
            new ProcessOutputReader(java.getInputStream(), System.out).start();
            new ProcessOutputReader(java.getErrorStream(), System.err).start();

            try {
                int rc = java.waitFor();
                if (rc != 0) {
                    StringBuilder err = new StringBuilder("Failed to execute this process (rc " + rc + "):");
                    for (String chunk : command) {
                        err.append(" ");
                        err.append(chunk);
                    }
                    throw new ProcessExecutionException(err.toString());
                }
            } catch (InterruptedException exc) {
            }
        }

        long end = System.currentTimeMillis();
        System.out.println();
        System.out.println("Wrote relationship details of " + populatedSchemas.size() + " schema" + (populatedSchemas.size() == 1 ? "" : "s") + " in " + (end - start) / 1000 + " seconds.");
        System.out.println("Start with " + new File(outputDir, "index.html"));
    }

    public void analyze(String dbName, List<String> schemas, List<String> args, Config config) throws SQLException, IOException {
        analyze(dbName, null, null, schemas, args, config);
    }

   private void writeIndexPage(String dbName, List<String> populatedSchemas, DatabaseMetaData meta, File outputDir, String charset) throws IOException {
        if (populatedSchemas.size() > 0) {
            LineWriter index = new LineWriter(new File(outputDir, "index.html"), charset);
            HtmlMultipleSchemasIndexPage.getInstance().write(dbName, populatedSchemas, meta, index);
            index.close();
        }
    }

    private List<String> getPopulatedSchemas(DatabaseMetaData meta, String schemaSpec, boolean isCatalog) throws SQLException {
        List<String> populatedSchemas;

        if ((!isCatalog && meta.supportsSchemasInTableDefinitions()) ||
             (isCatalog && meta.supportsCatalogsInTableDefinitions())) {
            Pattern schemaRegex = Pattern.compile(schemaSpec);

            populatedSchemas = DbAnalyzer.getPopulatedSchemas(meta, schemaSpec, isCatalog);
            Iterator<String> iter = populatedSchemas.iterator();
            while (iter.hasNext()) {
                String schema = iter.next();
                if (!schemaRegex.matcher(schema).matches()) {
                    if (fineEnabled) {
                        logger.fine("Excluding schema " + schema +
                                    ": doesn't match + \"" + schemaRegex + '"');
                    }
                    iter.remove(); // remove those that we're not supposed to analyze
                } else {
                    if (fineEnabled) {
                        logger.fine("Including schema " + schema +
                                    ": matches + \"" + schemaRegex + '"');
                    }
                }
            }
        } else {
            populatedSchemas = new ArrayList<String>();
        }

        return populatedSchemas;
    }

    private static class ProcessOutputReader extends Thread {
        private final Reader processReader;
        private final PrintStream out;

        ProcessOutputReader(InputStream processStream, PrintStream out) {
            processReader = new InputStreamReader(processStream);
            this.out = out;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                int ch;
                while ((ch = processReader.read()) != -1) {
                    out.print((char)ch);
                    out.flush();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    processReader.close();
                } catch (Exception exc) {
                    exc.printStackTrace(); // shouldn't ever get here...but...
                }
            }
        }
    }
}