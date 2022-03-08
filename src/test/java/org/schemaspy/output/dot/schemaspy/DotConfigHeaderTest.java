package org.schemaspy.output.dot.schemaspy;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.schemaspy.output.dot.DotConfig;

public class DotConfigHeaderTest {

    /**
     * Given a name,
     * When the object is asked for its header,
     * Then the response should contain the graph's name
     */
    @Test
    void interpretName() {
        MatcherAssert.assertThat(
                new DotConfigHeader(
                        new DotConfig() {
                            @Override
                            public boolean isRankDirBugEnabled() {
                                return false;
                            }

                            @Override
                            public String getFont() {
                                return "";
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
                        },
                        () -> "Graph",
                        false
                ).value(),
                CoreMatchers.containsString("digraph \"Graph\" {")
        );
    }

    /**
     * Given the name of a font,
     * When the object is asked for its header,
     * Then the response should contain the font.
     */
    @Test
    void interpretFonts() {
        MatcherAssert.assertThat(
                new DotConfigHeader(
                        new DotConfig() {
                            @Override
                            public boolean isRankDirBugEnabled() {
                                return false;
                            }

                            @Override
                            public String getFont() {
                                return "font";
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
                        },
                        "",
                        false
                ).value(),
                CoreMatchers.containsString("    fontname=\"font\"")
        );
    }

    /**
     * Given a request to show labels,
     * When the object is asked for its header,
     * Then the response should justify the label.
     */
    @Test
    void justifyLabel() {
        MatcherAssert.assertThat(
                new DotConfigHeader(
                        new DotConfig() {
                            @Override
                            public boolean isRankDirBugEnabled() {
                                return false;
                            }

                            @Override
                            public String getFont() {
                                return "";
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
                        },
                        "",
                        true
                ).value(),
                CoreMatchers.containsString("    labeljust=\"l\"")
        );
    }
}
