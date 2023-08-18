package org.schemaspy.progress;

import java.time.Duration;

/**
 * Abstraction for reporting or acting
 * upon the completion of a job, also
 * it's provided with the number of
 * progress calls and the duration of
 * the job
 */
public interface FinishCallback {

  /**
   *
   * @param increments total number of progressions during the job
   * @param duration for the execution of the job
   */
  void finished(long increments, Duration duration);
}
