package org.schemaspy.progress;

/**
 * Abstraction for the task of reporting progress
 */
public interface ProgressReporter {

  /**
   * The current progression to report
   * @param increments current number of progressions.
   */
  void report(long increments);
}
