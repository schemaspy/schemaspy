package org.schemaspy.progress;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.concurrent.atomic.LongAdder;

/**
 * Proc for reporting progressions conditionally
 * to avoid always reporting progress.
 * This is to avoid spamming the log.
 * Unattended don't want to see every progress
 * and attended just want to know that the
 * process isn't hanged.
 * @param <T> exception thrown by the job
 */
public class ConditionalProgress<T extends Throwable> implements Proc<T> {
  private final ProgressReporter progressReporter;
  private final FinishCallback finishCallback;
  private final Condition condition;
  private final Job<T> job;
  private final InstantSource instantSource;

  public ConditionalProgress(
      ProgressReporter progressReporter,
      FinishCallback finishCallback,
      Condition condition,
      Job<T> job,
      InstantSource instantSource
  ) {
    this.progressReporter = progressReporter;
    this.finishCallback = finishCallback;
    this.condition = condition;
    this.job = job;
    this.instantSource = instantSource;
  }

  /**
   * {@inheritDoc}
   */
  public void execute() throws T {
    final LongAdder increments = new LongAdder();
    final Instant start = instantSource.instant();
    job.execute(() -> {
      increments.increment();
      if (condition.report()) {
        progressReporter.report(increments.sum());
      }
    });
    finishCallback.finished(
        increments.sum(),
        Duration.between(
            start,
            instantSource.instant()
        )
    );
  }
}
