package org.schemaspy.progress;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.schemaspy.testing.ControlledInstanceSource;

import static org.hamcrest.MatcherAssert.assertThat;

class ConditionalProgressTest {

  @Test
  void timeTracking() {
    final Duration[] finished = new Duration[1];
    new ConditionalProgress<>(
        increments -> {},
        (increments, duration) -> finished[0] = duration,
        () -> true,
        progress -> {},
        new ControlledInstanceSource(Instant.now(), Duration.ofSeconds(10), 2)
    ).execute();
    assertThat(
        finished[0].toSeconds(),
        Matchers.equalTo(10L)
    );
  }

  @Test
  void progressCounting() {
    final long[] progressCalls = new long[1];
    new ConditionalProgress<>(
        increments -> {},
        (increments, duration) -> progressCalls[0] = increments,
        () -> true,
        progress -> {progress.progressed();progress.progressed();},
        Clock.systemDefaultZone()
    ).execute();
    assertThat(
        progressCalls[0],
        Matchers.equalTo(2L)
    );
  }

  @Test
  void willReportProgress() {
    final boolean[] reported = new boolean[]{false};
    new ConditionalProgress<>(
        increments -> reported[0] = true,
        (increments, duration) -> {},
        () -> true,
        progress -> {progress.progressed();progress.progressed();},
        Clock.systemDefaultZone()
    ).execute();
    assertThat(
        reported[0],
        Matchers.is(true)
    );
  }

  @Test
  void willNotReportProgress() {
    final boolean[] reported = new boolean[]{false};
    new ConditionalProgress<>(
        increments -> reported[0] = true,
        (increments, duration) -> {},
        () -> false,
        progress -> {progress.progressed();progress.progressed();},
        Clock.systemDefaultZone()
    ).execute();
    assertThat(
        reported[0],
        Matchers.is(false)
    );
  }
}