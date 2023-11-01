package org.schemaspy.output.dot.schemaspy.node.footer;

import org.schemaspy.output.dot.schemaspy.graph.Element;

public class Children implements Element {

  private final int count;
  private final boolean show;

  public Children(
      final int count,
      final boolean show
  ) {
    this.count = count;
    this.show = show;
  }

  @Override
  public String value() {
    if (count > 0 || show) {
      return count + " &gt;";
    } else {
      return "  ";
    }
  }
}
