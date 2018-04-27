/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.schemaspy.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class Dot {
    private static Dot instance = new Dot();
    private final GraphvizVersion graphvizVersion;
    private final GraphvizVersion supportedGraphvizVersion = new GraphvizVersion("2.26");
    private final GraphvizVersion badGraphvizVersion = new GraphvizVersion("2.31");
    private final String lineSeparator = System.getProperty("line.separator");
    private String dotExe;
    private String format = Config.getInstance().getImageFormat();
    private String renderer;
    private final Set<String> validatedRenderers = Collections.synchronizedSet(new HashSet<String>());
    private final Set<String> invalidatedRenderers = Collections.synchronizedSet(new HashSet<String>());

    private final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CAIRO_RENDERER = ":cairo";
    private static final String GD_RENDERER = ":gd";
    private static final String EMPTY_RENDERER = "";

    private Dot() {
        String versionText = null;
        // dot -V should return something similar to:
        //  dot graphvizVersion 2.8 (Fri Feb  3 22:38:53 UTC 2006)
        // or sometimes something like:
        //  dot - Graphviz graphvizVersion 2.9.20061004.0440 (Wed Oct 4 21:01:52 GMT 2006)
        String[] dotCommand = new String[]{getExe(), "-V"};

        try {
            Process process = Runtime.getRuntime().exec(dotCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String versionLine = reader.readLine();
            LOGGER.trace("GraphvizVersion: \"{}\"", versionLine);

            // look for a number followed numbers or dots
            Matcher matcher = Pattern.compile("[0-9]+\\.[0-9]+[^\\.]").matcher(versionLine);
            if (matcher.find()) {
                versionText = matcher.group();
            } else {
                if (Config.getInstance().isHtmlGenerationEnabled()) {
                    System.err.println();
                    LOGGER.warn("Invalid dot configuration detected.  '{}' returned:", getDisplayableCommand(dotCommand));
                    LOGGER.warn("   {}", versionLine);
                }
            }
        } catch (Exception validDotDoesntExist) {
            if (Config.getInstance().isHtmlGenerationEnabled()) {
                System.err.println();
                LOGGER.warn("Failed to query Graphviz graphvizVersion information");
                LOGGER.warn("  with: {}", getDisplayableCommand(dotCommand));
                LOGGER.warn("  {}", validDotDoesntExist);
                LOGGER.info("Graphviz query failure details:", validDotDoesntExist);
            }
        }

        graphvizVersion = new GraphvizVersion(versionText);
        validatedRenderers.add("");
    }

    public static Dot getInstance() {
        return instance;
    }

    public boolean exists() {
        return graphvizVersion.toString() != null;
    }

    public GraphvizVersion getGraphvizVersion() {
        return graphvizVersion;
    }

    public boolean isValid() {
        return exists() && (getGraphvizVersion().equals(supportedGraphvizVersion) || getGraphvizVersion().compareTo(badGraphvizVersion) > 0);
    }

    public String getSupportedVersions() {
        return "dot graphvizVersion " + supportedGraphvizVersion + " or versions greater than " + badGraphvizVersion;
    }

    public boolean supportsCenteredEastWestEdges() {
        return getGraphvizVersion().compareTo(new GraphvizVersion("2.6")) >= 0;
    }

    /**
     * Set the image format to generate.  Defaults to <code>png</code>.
     * See <a href='http://www.graphviz.org/doc/info/output.html'>http://www.graphviz.org/doc/info/output.html</a>
     * for valid formats.
     *
     * @param format image format to generate
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return
     * @see #setFormat(String)
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns true if the installed dot requires specifying :gd as a renderer.
     * This was added when Win 2.15 came out because it defaulted to Cairo, which produces
     * better quality output, but at a significant speed and size penalty.<p>
     * <p>
     * The intent of this property is to determine if it's ok to tack ":gd" to
     * the format specifier.  Earlier versions didn't require it and didn't know
     * about the option.
     *
     * @return
     */
    public boolean requiresGdRenderer() {
        return getGraphvizVersion().compareTo(new GraphvizVersion("2.12")) >= 0 && supportsRenderer(GD_RENDERER);
    }

    /**
     * Set the renderer to use for the -Tformat[:renderer[:formatter]] dot option as specified
     * at <a href='http://www.graphviz.org/doc/info/command.html'>
     * http://www.graphviz.org/doc/info/command.html</a> where "format" is specified by
     * {@link #setFormat(String)}<p>
     * Note that the leading ":" is required while :formatter is optional.
     *
     * @param renderer
     */
    public void setRenderer(String renderer) {
        if (isValid() && !supportsRenderer(renderer)) {
            LOGGER.info("renderer '{}' is not supported by your graphvizVersion of dot", renderer);
        }

        this.renderer = renderer;
    }

    /**
     * @return the renderer to use
     * @see #setRenderer(String)
     */
    public String getRenderer() {
        if (renderer == null) {
            setHighQuality(true);
        }

        return supportsRenderer(renderer) ? renderer : (requiresGdRenderer() ? GD_RENDERER : "");
    }

    /**
     * If <code>true</code> then generate output of "higher quality".
     * Note that the default is intended to be "higher quality",
     * but various installations of Graphviz may have have different abilities.
     * That is, some might not have the "lower quality" libraries and others might
     * not have the "higher quality" libraries.
     */
    public void setHighQuality(boolean highQuality) {
        if (highQuality && supportsRenderer(CAIRO_RENDERER)) {
            setRenderer(CAIRO_RENDERER);
        } else if (supportsRenderer(GD_RENDERER)) {
            setRenderer(GD_RENDERER);
        } else {
            setRenderer(EMPTY_RENDERER);
        }
    }

    /**
     * @see #setHighQuality(boolean)
     */
    public boolean isHighQuality() {
        return getRenderer().contains(CAIRO_RENDERER);
    }

    /**
     * Returns <code>true</code> if the specified renderer is supported.
     * See {@link #setRenderer(String)} for renderer details.
     *
     * @param renderer
     * @return
     */
    public boolean supportsRenderer(@SuppressWarnings("hiding") String renderer) {
        if (!exists())
            return false;

        if (validatedRenderers.contains(renderer))
            return true;

        if (invalidatedRenderers.contains(renderer))
            return false;

        try {
            String[] dotCommand = new String[]{
                    getExe(),
                    "-T" + getFormat() + ':'
            };
            Process process = Runtime.getRuntime().exec(dotCommand);
            BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = errors.readLine()) != null) {
                if (line.contains(getFormat() + renderer)) {
                    validatedRenderers.add(renderer);
                }
            }
            process.waitFor();
        } catch (Exception exc) {
            LOGGER.error("Error determining if a renderer is supported",exc);
        }

        if (!validatedRenderers.contains(renderer)) {
            LOGGER.info("Failed to validate {} renderer '{}'.  Reverting to default renderer for {}" + '.', getFormat(), renderer, getFormat());
            invalidatedRenderers.add(renderer);
            return false;
        }

        return true;
    }

    /**
     * Returns the executable to use to run dot
     *
     * @return
     */
    private String getExe() {
        if (dotExe == null) {
            File gv = Config.getInstance().getGraphvizDir();

            if (gv == null) {
                // default to finding dot in the PATH
                dotExe = "dot";
            } else {
                // pull dot from the Graphviz bin directory specified
                dotExe = new File(new File(gv, "bin"), "dot").toString();
            }
        }

        return dotExe;
    }

    /**
     * Using the specified .dot file generates an image returning the image's image map.
     */
    public String generateDiagram(File dotFile, File diagramFile) throws DotFailure {
        StringBuilder mapBuffer = new StringBuilder(1024);

        BufferedReader mapReader = null;
        // this one is for executing.  it can (hopefully) deal with funky things in filenames.
        String[] dotCommand = new String[]{
                getExe(),
                "-T" + getFormat() + getRenderer(),
                dotFile.toString(),
                "-o" + diagramFile,
                //"-v", //Enable verbose mode
                "-Tcmapx"
        };
        // this one is for display purposes ONLY.
        String commandLine = getDisplayableCommand(dotCommand);
        LOGGER.debug(commandLine);

        try {
            Process process = Runtime.getRuntime().exec(dotCommand);
            new ProcessOutputReader(commandLine, process.getErrorStream()).start();
            mapReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = mapReader.readLine()) != null) {
                mapBuffer.append(line);
                mapBuffer.append(lineSeparator);
            }
            int rc = process.waitFor();
            if (rc != 0)
                throw new DotFailure("'" + commandLine + "' failed with return code " + rc);
            if (!diagramFile.exists())
                throw new DotFailure("'" + commandLine + "' failed to create output file");

            // dot generates post-HTML 4.0.1 output...convert trailing />'s to >'s
            return mapBuffer.toString().replace("/>", ">");
        } catch (InterruptedException interrupted) {
            throw new RuntimeException(interrupted);
        } catch (DotFailure failed) {
            FileUtils.deleteQuietly(diagramFile);
            throw failed;
        } catch (IOException failed) {
            FileUtils.deleteQuietly(diagramFile);
            throw new DotFailure("'" + commandLine + "' failed with exception " + failed);
        } finally {
            IOUtils.closeQuietly(mapReader);
        }
    }

    public class DotFailure extends IOException {
        private static final long serialVersionUID = 3833743270181351987L;

        public DotFailure(String msg) {
            super(msg);
        }
    }

    private static String getDisplayableCommand(String[] command) {
        StringBuilder displayable = new StringBuilder();
        for (int i = 0; i < command.length; ++i) {
            displayable.append(command[i]);
            if (i + 1 < command.length)
                displayable.append(' ');
        }
        return displayable.toString();
    }

    private static class ProcessOutputReader extends Thread {
		private final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private final BufferedReader processReader;
        private final String command;

        ProcessOutputReader(String command, InputStream processStream) {
            processReader = new BufferedReader(new InputStreamReader(processStream));
            this.command = command;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = processReader.readLine()) != null) {
                    // don't report port id unrecognized or unrecognized port
                    if (!line.contains("unrecognized") && !line.contains("port"))
                        System.err.println(command + ": " + line);
                }
            } catch (IOException ioException) {
                LOGGER.error("Error reading from process",ioException);
            } finally {
                IOUtils.closeQuietly(processReader);
            }
        }
    }
}