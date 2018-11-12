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
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


public class VizJSDotTest {

    private static final String ENCODING = "UTF-8";
    private VizJSDot javaDotViz = new VizJSDot();

    @Test
    public void transformDotToSvg() throws Exception {
        URL sourceDot = VizJSDotTest.class.getResource("/vizjs/location.1degree.dot");
        URL expectedSvg = VizJSDotTest.class.getResource("/vizjs/location.1degree.svg");
        File diagramFile = File.createTempFile("test", "svg");
        javaDotViz.generateDiagram(new File(sourceDot.getFile()), diagramFile);
        String svgContent = IOUtils.toString(diagramFile.toURI(),"UTF-8");
        assertThat(svgContent).isEqualTo(IOUtils.toString(expectedSvg, ENCODING));
    }
}