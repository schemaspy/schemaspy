package org.schemaspy.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class WhenIf<T> implements Predicate<T> {

  private final Predicate<T> origin;
  private final Consumer<T> onTrue;
  private final Consumer<T> onFalse;

  public WhenIf(
      final Predicate<T> origin,
      final Consumer<T> onTrue,
      final Consumer<T> onFalse
  ) {
    this.origin = origin;
    this.onTrue = onTrue;
    this.onFalse = onFalse;
  }

  @Override
  public boolean test(final T t) {
    if (this.origin.test(t)) {
      this.onTrue.accept(t);
      return true;
    } else {
      this.onFalse.accept(t);
      return false;
    }
  }
}
