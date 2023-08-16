package org.schemaspy.output.dot.schemaspy.node;

import org.schemaspy.output.dot.schemaspy.graph.Element;

public class Footer implements Element {

  private static final String TEMPLATE =
      "<TABLE BORDER=\"0\" CELLBORDER=\"0\" CELLSPACING=\"0\">"
          + "<TR>"
          + "<TD ALIGN=\"LEFT\" BGCOLOR=\"%1$s\">%2$s</TD><VR/>"
          + "<TD ALIGN=\"CENTER\" BGCOLOR=\"%1$s\">%3$s</TD><VR/>"
          + "<TD ALIGN=\"RIGHT\" BGCOLOR=\"%1$s\">%4$s</TD>"
          + "</TR>"
          + "</TABLE>";

  private final String background;
  private final Element parents;
  private final Element rows;
  private final Element children;

  public Footer(
      final String background,
      final Element parents,
      final Element rows,
      final Element children
  ) {
    this.background = background;
    this.parents = parents;
    this.rows = rows;
    this.children = children;
  }

  @Override
  public String value() {
    return TEMPLATE.formatted(
        background,
        parents.value(),
        rows.value(),
        children.value()
    );
  }
}
