package org.schemaspy.logging;

public final class Sanitize {

  private final String text;

  public Sanitize(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text.replaceAll("[\r\n]","");
  }
}
