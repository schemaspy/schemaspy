package org.schemaspy.input.dbms.classloader;

import java.util.ArrayList;
import java.util.List;

public class ClSticky implements ClassloaderSource {

  private final ClassloaderSource origin;
  private final List<ClassLoader> cache;

  public ClSticky(final ClassloaderSource origin) {
    this(origin, new ArrayList<>());
  }

  public ClSticky(final ClassloaderSource origin, final List<ClassLoader> cache) {
    this.origin = origin;
    this.cache = cache;
  }

  @Override
  public ClassLoader classloader() {
    if (this.cache.isEmpty()) {
      this.cache.add(this.origin.classloader());
    }
    return this.cache.get(0);
  }
}
