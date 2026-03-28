package org.schemaspy.util;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * Tests for {@link JarFileRootPath}.
 */
class JarFileRootPathTest {

  /**
   * Given a JAR with url and entry,
   * When the object is asked for its path,
   * Then it should respond with the url.
   */
  @Test
  void parseUrl() {
    final String url = "www.example.com/example.jar";
    final String entry = "com/example/example.class";
    AssertionsForClassTypes.assertThat(
        new JarFileRootPath(String.format("%s!/%s", url, entry)).toPath()
    ).isEqualTo(
        Paths.get(url)
    );
  }

  /**
   * Given a JAR with uri and entry,
   * When the object is asked for its path,
   * Then it should respond with the uri.
   */
  @Test
  void parseUri() {
    final String uri = "/home/example/example.jar";
    final String entry = "com/example/";
    AssertionsForClassTypes.assertThat(
        new JarFileRootPath(String.format("%s!/%s", uri, entry)).toPath()
    ).isEqualTo(
        Paths.get(uri)
    );
  }

  /**
   * Given a whole JAR (no specific entry),
   * When the object is asked for its path,
   * Then it should respond with the uri.
   */
  @Test
  void parseWholeJar() {
    final String uri = "/home/example/example.jar";
    AssertionsForClassTypes.assertThat(
        new JarFileRootPath(String.format("%s!/", uri)).toPath()
    ).isEqualTo(
        Paths.get(uri)
    );
  }

  /**
   * Given an uri without JAR separator,
   * When the object is asked for its path,
   * Then it should respond with the uri.
   */
  @Test
  void parseNoJar() {
    final String uri = "/home/example/example.jar";
    AssertionsForClassTypes.assertThat(
        new JarFileRootPath(uri).toPath()
    ).isEqualTo(
        Paths.get(uri)
    );
  }
}
