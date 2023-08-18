package org.schemaspy.progress;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.schemaspy.testing.ControlledInstanceSource;

import static org.hamcrest.MatcherAssert.assertThat;

class IfUpdateAfterTest {

  @Test
  void earlyFalse() {
    assertThat(
        new IfUpdateAfter(
            Duration.ofSeconds(1),
            Clock.fixed(
                Clock.systemDefaultZone().instant(),
                Clock.systemDefaultZone().getZone()
            )
        ).report(),
        Matchers.is(false)
    );
  }

  @Test
  void lateTrue() {
    assertThat(
        new IfUpdateAfter(
            Duration.ofSeconds(1),
            new ControlledInstanceSource(Instant.now(), Duration.ofSeconds(11), 3)
        ).report(),
        Matchers.is(true)
    );
  }

  @Test
  void nextRelativeToCheckEarlyIsFalse() {
    Instant start = Instant.now();
    Instant advance = start.plus(Duration.ofSeconds(20));
    Instant checkFalse = advance.plus(Duration.ofSeconds(9));
    IfUpdateAfter ifUpdateAfter = new IfUpdateAfter(
        Duration.ofSeconds(10),
        new ControlledInstanceSource(start, advance, checkFalse)
    );
    assertThat(ifUpdateAfter.report(), Matchers.is(true));
    assertThat(ifUpdateAfter.report(), Matchers.is(false));
  }

  @Test
  void nextRelativeToCheckLateIsTrue() {
    Instant start = Instant.now();
    Instant advance = start.plus(Duration.ofSeconds(20));
    Instant checkTrue = advance.plus(Duration.ofSeconds(11));
    IfUpdateAfter ifUpdateAfter = new IfUpdateAfter(
        Duration.ofSeconds(10),
        new ControlledInstanceSource(start, advance, checkTrue)
    );
    assertThat(ifUpdateAfter.report(), Matchers.is(true));
    assertThat(ifUpdateAfter.report(), Matchers.is(true));
  }

}