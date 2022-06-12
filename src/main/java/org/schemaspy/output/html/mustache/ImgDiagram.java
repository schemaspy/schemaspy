package org.schemaspy.output.html.mustache;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgDiagram implements Diagram {

    private static final Pattern MAP_NAME_PATTERN = Pattern.compile("<map.*name=\"([\\w\\s]+).*");
    private static final String PNG_TEMPLATE =
    "<img id=\"%s\" src=\"%s\" usemap=\"#%s\" style=\"max-width:100%%;\" border=\"0\" align=\"top\">%n%s";

    private final String id;
    private final String source;
    private final String map;

    public ImgDiagram(String id, String source, String map) {
        this.id = id;
        this.source = source;
        this.map = map;
    }

    @Override
    public String html() {
        return String.format(PNG_TEMPLATE, id, source, mapName(), map());
    }

    private String mapName() {
        Matcher matcher = MAP_NAME_PATTERN.matcher(map());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String map() {
        if (Objects.isNull(map)) {
            return "";
        }
        return map.trim();
    }
}
