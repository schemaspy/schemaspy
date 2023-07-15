package org.schemaspy.input.dbms;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DbDriverLoaderErrorMessage {

    private final String[] driverClass;
    private final String driverPath;
    private final Set<URI> classpath;
    private final ConnectionConfig connectionConfig;

    public DbDriverLoaderErrorMessage(
            final String []driverClass,
            final String driverPath,
            final Set<URI> classpath,
            final ConnectionConfig connectionConfig
    ) {
        this.driverClass = driverClass;
        this.driverPath = driverPath;
        this.classpath = classpath;
        this.connectionConfig = connectionConfig;
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder()
                .append("Failed to create any of '")
                .append(Arrays.stream(driverClass).collect(Collectors.joining(", ")))
                .append("' driver from driverPath '")
                .append(driverPath)
                .append("' with sibling jars ")
                .append((connectionConfig.withLoadSiblings() ? "yes" : "no"))
                .append(".")
                .append(System.lineSeparator())
                .append("Resulting in classpath:");
        if (classpath.isEmpty()) {
            sb.append(" empty").append(System.lineSeparator());
        } else {
            sb.append(System.lineSeparator());
            for (URI uri : classpath) {
                sb.append("\t").append(uri.toString()).append(System.lineSeparator());
            }
        }
        sb.append(missingPathsMessage());
        return sb.toString();
    }

    public String missingPathsMessage() {
        StringBuilder sb = new StringBuilder();
        List<String> missingPaths = getMissingPaths(driverPath);
        if (!missingPaths.isEmpty()) {
            sb.append("There were missing paths in driverPath:").append(System.lineSeparator());
            for (String path : missingPaths) {
                sb.append("\t").append(path).append(System.lineSeparator());
            }
            sb
                    .append("Use commandline option '-dp' to specify driver location.");
        }
        return sb.toString();
    }

    /**
     * Returns a list of {@link File}s in <code>path</code> that do not exist.
     * The intent is to aid in diagnosing invalid paths.
     *
     * @param path
     * @return
     */
    private List<String> getMissingPaths(String path) {
        List<String> missingFiles = new ArrayList<>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            if (!new File(piece).exists())
                missingFiles.add(piece);
        }

        return missingFiles;
    }
}
