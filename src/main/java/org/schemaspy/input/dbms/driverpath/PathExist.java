package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Path;
import java.util.function.Predicate;

public class PathExist implements Predicate<Path> {
  @Override
  public boolean test(final Path path) {
    return path.toFile().exists();
  }
}
