package org.schemaspy.output.dot.schemaspy.node.footer;

import org.schemaspy.output.dot.schemaspy.graph.Element;

public class Parents implements Element {
  private final int count;
  private final boolean show;

  public Parents(
      final int count,
      final boolean show
  ) {
    this.count = count;
    this.show = show;
  }

  @Override
  public String value() {
    if (count > 0 || show) {
      return "&lt; " + count;
    } else {
      return "  ";
    }
  }
}
