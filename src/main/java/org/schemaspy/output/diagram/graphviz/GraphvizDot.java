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
package org.schemaspy.output.diagram.graphviz;

import org.schemaspy.logging.Sanitize;
import org.schemaspy.output.diagram.RenderException;
import org.schemaspy.output.diagram.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class GraphvizDot implements Renderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GraphvizConfig graphvizConfig;
    private final GraphvizVersion graphvizVersion;
    private final GraphvizVersion badGraphvizVersion = new GraphvizVersion("2.31");
    private final String lineSeparator = System.getProperty("line.separator");
    private String dotExe;
    private final Set<String> validRenders = new HashSet<>();

    private final String effectiveRenderer;
    private static final String CAIRO_RENDERER = ":cairo";
    private static final String GD_RENDERER = ":gd";
    private static final String EMPTY_RENDERER = "";

    public GraphvizDot(GraphvizConfig graphvizConfig) {
        this.graphvizConfig = graphvizConfig;
        this.graphvizVersion = initVersion();
        initValidRenders();
        effectiveRenderer = initRenderer();
        LOGGER.info("Graphviz renderer set to '{}'", effectiveRenderer);
    }

    private GraphvizVersion initVersion() {
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
            Matcher matcher = Pattern.compile("([0-9]+\\.)+[0-9]+[^.]").matcher(versionLine);
            if (matcher.find()) {
                versionText = matcher.group();
            } else {
                LOGGER.warn("Invalid dot configuration detected. '{}' returned: '{}'", getDisplayableCommand(dotCommand), versionLine);
            }
        } catch (Exception validDotDoesntExist) {
            LOGGER.warn("Failed to query Graphviz version using '{}'", getDisplayableCommand(dotCommand), validDotDoesntExist);
        }

        return new GraphvizVersion(versionText);
    }

    private void initValidRenders() {
        if (!isValid()) {
            return;
        }
        Pattern rendererPattern = Pattern.compile(format() + "(:[^\"][a-zA-Z]*)");
        try {
            String[] dotCommand = new String[]{
                    getExe(),
                "-T" + format() + ':'
            };
            Process process = Runtime.getRuntime().exec(dotCommand);
            try (Scanner scanner = new Scanner(new InputStreamReader(process.getErrorStream()))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    Matcher m = rendererPattern.matcher(line);
                    while (m.find()) {
                        validRenders.add(m.group(1));
                    }
                }
            }
            process.waitFor();
        } catch (InterruptedException ie) {
            LOGGER.error("Interrupted", ie);
            Thread.currentThread().interrupt();
        } catch (Exception exc) {
            LOGGER.error("Error determining available renders",exc);
        }
    }

    public boolean isValid() {
        return exists() && (getGraphvizVersion().compareTo(badGraphvizVersion) != 0);
    }

    public boolean exists() {
        return graphvizVersion.toString() != null;
    }

    /**
     * Set the renderer to use for the -Tformat[:renderer[:formatter]] dot option as specified
     * at <a href='http://www.graphviz.org/doc/info/command.html'>
     * http://www.graphviz.org/doc/info/command.html</a> where "format" is specified by getFormat
     * Note that the leading ":" is required while :formatter is optional.
     *
     * @return renderer
     */
    private String initRenderer() {
        String possibleRenderer = graphvizConfig.getRenderer();
        if (Objects.nonNull(possibleRenderer) && validRenders.contains(possibleRenderer)) {
            if (validRenders.contains(possibleRenderer)) {
                return possibleRenderer;
            }
            LOGGER.warn("Specified renderer '{}' is not supported, available renders are {}", possibleRenderer, validRenders);
        }
        if (graphvizConfig.isLowQuality() && validRenders.contains(GD_RENDERER)) {
            return GD_RENDERER;
        }
        if (validRenders.contains(CAIRO_RENDERER)) {
            return CAIRO_RENDERER;
        }
        return EMPTY_RENDERER;
    }

    public String identifier() {
        return "Graphviz dot " + getGraphvizVersion().toString();
    }

    public GraphvizVersion getGraphvizVersion() {
        return graphvizVersion;
    }

    /**
     * Get the image format to generate.  Defaults to <code>png</code>.
     * See <a href='http://www.graphviz.org/doc/info/output.html'>http://www.graphviz.org/doc/info/output.html</a>
     * for valid formats.
     *
     * @return image format to generate
     */
    public String format() {
        return Objects.isNull(graphvizConfig.getImageFormat()) ? "png": graphvizConfig.getImageFormat();
    }

    /**
     * Returns the executable to use to run dot
     *
     * @return
     */
    private String getExe() {
        if (dotExe == null) {

            if (Objects.isNull(graphvizConfig.getGraphvizDir())) {
                // default to finding dot in the PATH
                dotExe = "dot";
            } else {
                // pull dot from the Graphviz bin directory specified
                File gv = new File(graphvizConfig.getGraphvizDir());
                dotExe = new File(new File(gv, "bin"), "dot").toString();
            }
        }

        return dotExe;
    }

    /**
     * Using the specified .dot file generates an image returning the image's image map.
     */
    public String render(File dotFile, File diagramFile) {
        if (!isValid()) {
            throw new RenderException("Dot missing or invalid version");
        }
        StringBuilder mapBuffer = new StringBuilder(1024);

        ArrayList<String> dotCommands = new ArrayList<>();
        dotCommands.add(getExe());
        if ("svg".equalsIgnoreCase(format())) {
            dotCommands.add("-Tsvg");
        } else {
            dotCommands.add("-T" + format() + effectiveRenderer);
        }
        dotCommands.add(dotFile.getName());
        dotCommands.add("-o" + diagramFile.getName());
        if (!"svg".equalsIgnoreCase(format())) {
            dotCommands.add("-Tcmapx");
        }
        // this one is for executing.  it can (hopefully) deal with funky things in filenames.
        String[] dotCommand = dotCommands.toArray(new String[0]);
        // this one is for display purposes ONLY.
        String commandLine = getDisplayableCommand(dotCommand);
        LOGGER.debug(commandLine);

        try {
            Process process = Runtime.getRuntime().exec(dotCommand, null, dotFile.getParentFile());
            new ProcessOutputReader(commandLine, process.getErrorStream()).start();
            try (BufferedReader mapReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = mapReader.readLine()) != null) {
                    mapBuffer.append(line);
                    mapBuffer.append(lineSeparator);
                }
            }
            int rc = process.waitFor();
            if (rc != 0) {
                throw new RenderException("'" + commandLine + "' failed with return code " + rc);
            }
            if (!diagramFile.exists()) {
                throw new RenderException("'" + commandLine + "' failed to create output file");
            }

            // dot generates post-HTML 4.0.1 output...convert trailing />'s to >'s
            return mapBuffer.toString().replace("/>", ">");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RenderException("Interrupted during execution", interrupted);
        } catch (RenderException | IOException exception) {
            try {
                Files.deleteIfExists(diagramFile.toPath());
            } catch (IOException ioexception) {
                LOGGER.debug("Failed to delete '{}'", diagramFile, ioexception);
            }
            throw new RenderException("'" + commandLine + "' failed with exception " + exception);
        }
    }

    private static String getDisplayableCommand(String[] command) {
        StringBuilder displayable = new StringBuilder();
        for (int i = 0; i < command.length; ++i) {
            displayable.append(command[i]);
            if (i + 1 < command.length) {
                displayable.append(' ');
            }
        }
        return displayable.toString();
    }

    private static class ProcessOutputReader extends Thread {
		private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private final InputStream processStream;
        private final String command;

        ProcessOutputReader(String command, InputStream processStream) {
            this.processStream = processStream;
            this.command = command;
            setDaemon(true);
        }

        @Override
        public void run() {
            try (BufferedReader processReader = new BufferedReader(new InputStreamReader(processStream))){
                String line;
                while ((line = processReader.readLine()) != null) {
                    // don't report port id unrecognized or unrecognized port
                    if (!line.contains("unrecognized") && !line.contains("port")) {
                        LOGGER.warn("{}: {}", new Sanitize(command), new Sanitize(line));
                    }
                }
            } catch (IOException ioException) {
                LOGGER.error("Error reading from process",ioException);
            }
        }
    }
}
