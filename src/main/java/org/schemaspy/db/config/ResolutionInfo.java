/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nils Petzaell
 */
public class ResolutionInfo {
    private String requested;
    private List<URL> trace = new ArrayList<>();

    private String workDir = System.getProperty("user.dir") + File.separator;

    public ResolutionInfo(String requested) {
        this.requested = requested;
    }

    public void addTrace(URL url) {
        trace.add(url);
    }

    public String getTrace() {
        return trace.stream().map(u ->
                u.getPath()
                        .replace("file:", "")
                        .replace(workDir, "")
        ).collect(
                Collectors.joining(
                        " ->" + System.lineSeparator() + "\t",
                        "Resolving dbType: " + requested + " ->" + System.lineSeparator() + "\t",
                        ""));
    }
}
