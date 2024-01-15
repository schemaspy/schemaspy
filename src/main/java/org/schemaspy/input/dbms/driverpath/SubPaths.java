package org.schemaspy.input.dbms.driverpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

public class SubPaths implements Iterable<Path>{

  private final Path path;

  public SubPaths(final Path path) {
    this.path = path;
  }

  @Override
  public Iterator<Path> iterator() {
    try (Stream<Path> pathStream = Files.walk(this.path)){
      return pathStream.sorted().toList().iterator();
    } catch (IOException e) {
      throw new RuntimeIOException("Unable to expand driver path", e);
    }
  }
}
