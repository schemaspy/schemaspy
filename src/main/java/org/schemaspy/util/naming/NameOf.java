package org.schemaspy.util.naming;

public class NameOf implements Name {

  private final String value;

  public NameOf(final String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }
}
