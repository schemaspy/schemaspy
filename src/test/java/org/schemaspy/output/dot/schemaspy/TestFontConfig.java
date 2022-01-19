package org.schemaspy.output.dot.schemaspy;

public class TestFontConfig implements FontConfig {
    @Override
    public String name() {
        return "Helvetica";
    }

    @Override
    public int size() {
        return 11;
    }

    @Override
    public int widthOfText(String text) {
        return 6 * text.length();
    }
}
