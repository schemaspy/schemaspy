package org.schemaspy.util.iterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class Filtered<T> implements Iterator<T> {

  private final Iterator<T> origin;
  private final Predicate<T> predicate;
  private final LinkedList<T> buffer;

  public Filtered(
      final Iterator<T> origin,
      final Predicate<T> predicate
  ) {
    this.origin = origin;
    this.predicate = predicate;
    this.buffer = new LinkedList<>();
  }

  @Override
  public boolean hasNext() {
    while (this.buffer.isEmpty() && this.origin.hasNext()) {
      final T nextElement = this.origin.next();
      if (predicate.test(nextElement)) {
        buffer.add(nextElement);
      }
    }
    return !this.buffer.isEmpty();
  }

  @Override
  public T next() {
    if (this.hasNext()) {
      return buffer.removeFirst();
    } else {
      throw new NoSuchElementException();
    }
  }
}
