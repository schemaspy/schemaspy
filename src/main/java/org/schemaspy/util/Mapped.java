package org.schemaspy.util;

import java.util.Iterator;
import java.util.function.Function;

public class Mapped<T,R> implements Iterable<R> {

  private final Iterable<T> origin;
  private final Function<T,R> mapFunction;

  public Mapped(
      final Iterable<T> origin,
      final Function<T, R> mapFunction
  ) {
    this.origin = origin;
    this.mapFunction = mapFunction;
  }

  @Override
  public Iterator<R> iterator() {
    return new org.schemaspy.util.iterator.Mapped(this.origin.iterator(), this.mapFunction);
  }
}
