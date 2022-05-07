package org.schemaspy.output.dot.schemaspy;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.schemaspy.output.dot.DotConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class DotOrphanFormatterTest {

    /**
     * Given a header,
     * When the object is asked to write itself,
     * Then the response should contain the header
     */
    @Test
    void writeHeader() {
        final Writer writer = new StringWriter();
        final String key = "header";
        final DotOrphanFormatter sut = new DotOrphanFormatter(
                new PrintWriter(writer),
                () -> key,
                () -> ""
        );
        sut.writeOrphan();
        MatcherAssert.assertThat(
                writer.toString(),
                CoreMatchers.containsString(key)
        );
    }

    /**
     * Given a node,
     * When the object is asked to write itself,
     * Then the response should contain the node
     */
    @Test
    void writeNode() {
        final Writer writer = new StringWriter();
        final String key = "node";
        final DotOrphanFormatter sut = new DotOrphanFormatter(
                new PrintWriter(writer),
                () -> "",
                () -> key
        );
        sut.writeOrphan();
        MatcherAssert.assertThat(
                writer.toString(),
                CoreMatchers.containsString(key)
        );
    }
}
