package org.schemaspy.progress;

/**
 * Abstraction of body of work that should track progress.
 * @param <T> exception that can be thrown by the job
 */
public interface Job<T extends Throwable> {

  /**
   *
   * @param progress {@link Progress}
   * @throws T exception thrown by job
   */
  void execute(Progress progress) throws T;

}
