package org.schemaspy.output.html.mustache;

public class SvgDiagram implements DiagramElement {

    private static final String SVG_TEMPLATE =
        "<object type=\"image/svg+xml\" id=\"%s\" data=\"%s\" style=\"max-width:100%%;\" border=\"0\" align=\"top\"></object>";

    private final String id;
    private final String source;

    public SvgDiagram(String id, String source) {
        this.id = id;
        this.source = source;
    }

    @Override
    public String html() {
        return String.format(SVG_TEMPLATE, id, source);
    }
}
