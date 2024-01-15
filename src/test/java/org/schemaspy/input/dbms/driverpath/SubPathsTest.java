package org.schemaspy.input.dbms.driverpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubPathsTest {

  private static final Path testPath = Paths.get("src", "test", "resources", "subPaths");

  @BeforeAll
  static void createEmptyDir() throws IOException {
    Path emptyDir = testPath.resolve("emptyDir");
    if (!emptyDir.toFile().exists()) {
      Files.createDirectories(emptyDir);
    }
  }

  @Test
  void expandDriverPathWithSubPaths() {
    assertThat(
        new SubPaths(testPath)
    ).containsExactly(
        testPath,
        testPath.resolve("emptyDir"),
        testPath.resolve("other"),
        testPath.resolve("other").resolve("otherSub"),
        testPath.resolve("other").resolve("otherSub").resolve("otherSub.properties"),
        testPath.resolve("some.properties")
    );
  }

  @Test
  void subPathsForOneFileReturnsSingleFileOnly() {
    assertThat(
        new SubPaths(testPath.resolve("some.properties"))
    ).containsExactly(
        testPath.resolve("some.properties")
    );
  }

}