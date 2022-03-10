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
        final DotOrphanFormatter sut = new DotOrphanFormatter(
                new DotConfig() {
                    @Override
                    public boolean isRankDirBugEnabled() {
                        return false;
                    }

                    @Override
                    public String getFont() {
                        return null;
                    }

                    @Override
                    public int getFontSize() {
                        return 0;
                    }

                    @Override
                    public int getTextWidth(String text) {
                        return 0;
                    }

                    @Override
                    public boolean useRelativeLinks() {
                        return false;
                    }

                    @Override
                    public boolean isNumRowsEnabled() {
                        return false;
                    }

                    @Override
                    public boolean isOneOfMultipleSchemas() {
                        return false;
                    }
                }
        );
        final Writer writer = new StringWriter();
        final String key = "header";
        sut.writeOrphan(
                new PrintWriter(writer),
                () -> key,
                () -> ""
        );
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
        final DotOrphanFormatter sut = new DotOrphanFormatter(
                new DotConfig() {
                    @Override
                    public boolean isRankDirBugEnabled() {
                        return false;
                    }

                    @Override
                    public String getFont() {
                        return null;
                    }

                    @Override
                    public int getFontSize() {
                        return 0;
                    }

                    @Override
                    public int getTextWidth(String text) {
                        return 0;
                    }

                    @Override
                    public boolean useRelativeLinks() {
                        return false;
                    }

                    @Override
                    public boolean isNumRowsEnabled() {
                        return false;
                    }

                    @Override
                    public boolean isOneOfMultipleSchemas() {
                        return false;
                    }
                }
        );
        final Writer writer = new StringWriter();
        final String key = "node";
        sut.writeOrphan(
                new PrintWriter(writer),
                () -> "",
                () -> key
        );
        MatcherAssert.assertThat(
                writer.toString(),
                CoreMatchers.containsString(key)
        );
    }
}
