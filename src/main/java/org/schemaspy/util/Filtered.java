package org.schemaspy.util;

import java.util.Iterator;
import java.util.function.Predicate;

public class Filtered<T> implements Iterable<T> {

  private final Iterable<T> origin;
  private final Predicate<T> predicate;

  public Filtered(
      final Iterable<T> origin,
      final Predicate<T> predicate
  ) {
    this.origin = origin;
    this.predicate = predicate;
  }

  @Override
  public Iterator<T> iterator() {
    return new org.schemaspy.util.iterator.Filtered<>(this.origin.iterator(), this.predicate);
  }

}
