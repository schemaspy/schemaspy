package org.schemaspy.input.dbms.driverpath;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DpFromIterableTest {

  @Test
  void somePaths() {
    assertThat(
      new DpFromIterable(
        List.of(
          Paths.get("src", "main"),
          Paths.get("src", "test")
        )
      ).value()
    ).isEqualTo(
      "src"
      + File.separator
      + "main"
      + File.pathSeparator
      + "src"
      + File.separator
      + "test"
    );
  }

  @Test
  void noPaths() {
    assertThat(
      new DpFromIterable(
        () -> Collections.emptyIterator()
      ).value()
    ).isEqualTo("");
  }

}