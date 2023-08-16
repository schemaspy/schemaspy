package org.schemaspy.output.dot.schemaspy.node.footer;

import java.text.NumberFormat;

import org.schemaspy.output.dot.schemaspy.graph.Element;

public class Rows implements Element {

  private final long count;
  private final boolean enabled;

  public Rows(
      final long count,
      final boolean enabled
  ) {
    this.count = count;
    this.enabled = enabled;
  }

  @Override
  public String value() {
    StringBuilder builder = new StringBuilder();
    if (enabled && count >= 0) {
      builder.append(NumberFormat.getInstance().format(count));
      builder.append(" row");
      if (count != 1)
        builder.append('s');
    } else {
      builder.append("  ");
    }
    return builder.toString();
  }
}
