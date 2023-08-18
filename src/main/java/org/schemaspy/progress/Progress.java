package org.schemaspy.progress;

/**
 * Abstraction to allow a {@link Job} to track progression
 */
public interface Progress {
  /**
   * Increment the number of progressions performed during the job.
   */
  void progressed();

}
