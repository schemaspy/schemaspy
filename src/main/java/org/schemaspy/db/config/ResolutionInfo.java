/*
 * Copyright (C) 2017 Nils Petzaell
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
