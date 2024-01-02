package org.schemaspy.util.markup;

public class MarkupFromString implements Markup {

  private final String markupText;

  public MarkupFromString(final String markupText) {
    this.markupText = markupText;
  }

  @Override
  public String value() {
    return markupText;
  }
}
