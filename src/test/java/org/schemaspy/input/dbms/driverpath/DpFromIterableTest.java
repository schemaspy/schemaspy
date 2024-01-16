package org.schemaspy.input.dbms.driverpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DpFromIterableTest {

  private static final Path basePath = Paths.get("src", "test", "resources", "subPaths");
  @BeforeAll
  static void createEmptyDir() throws IOException {
    Path emptyDir = basePath.resolve("emptyDir");
    if (!emptyDir.toFile().exists()) {
      Files.createDirectories(emptyDir);
    }
  }

  @Test
  void somePathsAndAMissing() {
    assertThat(
      new DpFromIterable(
        List.of(
          basePath,
          Paths.get("src", "nobueno")
        )
      )
    ).containsExactly(
      basePath,
      basePath.resolve("emptyDir"),
      basePath.resolve("other"),
      basePath.resolve("other").resolve("otherSub"),
      basePath.resolve("other").resolve("otherSub").resolve("otherSub.properties"),
      basePath.resolve("some.properties")
    );
  }

  @Test
  void noPaths() {
    assertThat(
      new DpFromIterable(Collections::emptyIterator
      )
    ).isEmpty();
  }

}