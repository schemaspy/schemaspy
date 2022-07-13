package org.schemaspy.output.dot.schemaspy.graph;

import org.schemaspy.output.dot.schemaspy.Header;
import org.schemaspy.util.naming.Name;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Digraph implements Graph {

    private final Name name;
    private final Header header;
    private final Element[] contents;

    public Digraph(final Name name, final Header header, final Element... contents) {
        this.name = name;
        this.header = header;
        this.contents = contents;
    }

    @Override
    public String dot() {
        return String.format(
                "digraph \"%s\" {%n%s%n%s%n}",
                this.name.value(),
                this.header.value(),
                Arrays.stream(this.contents)
                        .map(Element::value)
                        .collect(Collectors.joining(System.lineSeparator()))
        );
    }
}
