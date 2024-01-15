package org.schemaspy.util;

import java.util.Arrays;
import java.util.Iterator;

import org.schemaspy.util.iterator.Mapped;

public class Joined<T> implements Iterable<T> {

  private final Iterable<Iterable<T>> iterables;

  public Joined(final Iterable<T>...iterables) {
    this(() -> Arrays.asList(iterables).iterator());
  }
  public Joined(final Iterable<Iterable<T>> iterables) {
    this.iterables = iterables;
  }
  @Override
  public Iterator<T> iterator() {
    return new org.schemaspy.util.iterator.Joined<>(
        new Mapped<>(
            this.iterables.iterator(),
            Iterable::iterator
        )
    );
  }
}
