package org.schemaspy.util;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


public class JDotTest {

    private static final String ENCODING = "UTF-8";
    private JDot javaDotViz = new JDot("report");

    @Test
    public void transformDotToSvg() throws Exception {
        URL sourceDot = JDotTest.class.getResource("/vizjs/location.1degree.dot");
        URL expectedSvg = JDotTest.class.getResource("/vizjs/location.1degree.svg");
        File diagramFile = File.createTempFile("test", "svg");
        javaDotViz.renderDotByJvm(new File(sourceDot.getFile()), diagramFile);
        String svgContent = IOUtils.toString(diagramFile.toURI(),"UTF-8");
        assertThat(svgContent).isEqualTo(IOUtils.toString(expectedSvg, ENCODING));
    }
}