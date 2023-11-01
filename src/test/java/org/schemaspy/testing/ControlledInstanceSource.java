package org.schemaspy.testing;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

public class ControlledInstanceSource implements InstantSource {
  private final Iterator<Instant> instants;

  public ControlledInstanceSource(final Instant...instants) {
    this(Arrays.stream(instants).iterator());
  }

  public ControlledInstanceSource(final Instant instant, final Duration increments, int count) {
    this(IntStream.range(1, count+1).mapToObj(increments::multipliedBy).map(instant::plus).iterator());
  }

  public ControlledInstanceSource(Iterator<Instant> instants) {
    this.instants = instants;
  }

  @Override
  public Instant instant() {
    return instants.next();
  }
}
