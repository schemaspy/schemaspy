package org.schemaspy.progress;

/**
 * Abstraction of a condition.
 * If progress should be reported or not.
 */
public interface Condition {

  /**
   * Evaluate if we should report progress.
   * @return true report, false do not report
   */
  boolean report();
}
