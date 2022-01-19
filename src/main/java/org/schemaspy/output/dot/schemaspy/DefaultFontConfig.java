package org.schemaspy.output.dot.schemaspy;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

public class DefaultFontConfig implements FontConfig {

    private final AffineTransform affineTransform = new AffineTransform();
    private final FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, true);

    private final String name;
    private final int size;
    private final Font font;

    public DefaultFontConfig(String fontName, int fontSize) {
        this.name = fontName;
        this.size = fontSize;
        font = new Font(fontName, Font.BOLD, fontSize +1);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int widthOfText(String text) {
        return (int) (font.getStringBounds(text, fontRenderContext).getWidth());
    }
}
