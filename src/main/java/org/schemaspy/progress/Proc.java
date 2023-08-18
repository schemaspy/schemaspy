package org.schemaspy.progress;

/**
 * Abstraction of a process that throws a check exception
 * @param <T> generics for checked exception
 */
public interface Proc<T extends Throwable> {

  /**
   * Execute the Proc
   * @throws T checked Exception
   */
  void execute() throws T;
}
