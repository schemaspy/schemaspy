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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


public class VizJSDotTest {
    private VizJSDot javaDotViz = new VizJSDot();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void transformDotToSvg() throws Exception {
        Path vizJSPath = Paths.get("src","test","resources","vizjs");
        File diagramFile = temporaryFolder.newFile("location.1degree.svg");
        javaDotViz.generateDiagram(vizJSPath.resolve("location.1degree.dot").toFile(), diagramFile);
        assertThat(diagramFile.toPath()).hasSameContentAs(vizJSPath.resolve("location.1degree.svg"), StandardCharsets.UTF_8);
    }
}