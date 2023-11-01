package org.schemaspy.progress;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;

/**
 * Implementation of a Condition that only
 * returns true if a certain amount of time
 * has passed since the previous reporting.
 */

public class IfUpdateAfter implements Condition {

  private final InstantSource instantSource;
  private final Duration updateFrequency;
  private Instant nextUpdate;

  public IfUpdateAfter(
      final Duration updateFrequency,
      final InstantSource instantSource
  ) {
    this.updateFrequency = updateFrequency;
    this.instantSource = instantSource;
    this.nextUpdate = this.instantSource
        .instant()
        .plus(this.updateFrequency);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean report() {
    final Instant now = this.instantSource.instant();
    if (now.isAfter(this.nextUpdate)) {
      this.nextUpdate = now.plus(this.updateFrequency);
      return true;
    }
    return false;
  }

}
