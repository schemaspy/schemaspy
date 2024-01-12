package org.schemaspy.cli;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class FilePathSeparatorTest {

  @ParameterizedTest(name = "{0} should be {2} element(s) as {1}")
  @MethodSource("splitArguments")
  void split(String input, List<String> output, int size) {
    assertThat(
      new FilePathSeparator()
        .split(input)
    )
      .containsAll(output)
      .hasSize(size);
  }

  static Stream<Arguments> splitArguments() {
    return Stream.of(
      Arguments.arguments(
        "a,b,c",
        Named.named(
          "[\"a,b,c\"]",
          List.of("a,b,c")
        ),
        1
      ),
      Arguments.arguments(
        join("a", "b", "c"),
        List.of("a", "b", "c"),
        3
      ),
      Arguments.arguments(
        joinOther("a", "b", "c"),
        Named.named(
          "[\""+ joinOther("a","b","c") + "\"]",
          List.of(joinOther("a", "b", "c")
          )
        ),
        1
      ),
      Arguments.arguments(
        join("/pathOne/", "/pathTwo", "/pathThree"),
        List.of("/pathOne/", "/pathTwo", "/pathThree"),
        3
      )
    );
  }

  private static String join(String...elements) {
    return join(
      File.pathSeparatorChar,
      elements
    );
  }

  private static String joinOther(String...elements) {
    return join(
      ':' == File.pathSeparatorChar ? ';' : ':',
      elements
    );
  }

  private static String join(char separator, String...elements) {
    return String.join(
      ""+separator,
      elements
    );
  }

}