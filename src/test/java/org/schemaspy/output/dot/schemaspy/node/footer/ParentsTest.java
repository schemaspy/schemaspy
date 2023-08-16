package org.schemaspy.output.dot.schemaspy.node.footer;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;

class ParentsTest {

  @ParameterizedTest(name = "{index} ==> When show:{1} and count:{0} then output: {2}")
  @CsvSource({
      "0, true, '&lt; 0'",
      "0, false, '  '",
      "10, true, '&lt; 10'",
      "10, false, '&lt; 10'"
  })
  void children(int count, boolean show, String expected) {
    assertThat(
        new Parents(count, show).value(),
        new IsEqual<>( expected)
    );
  }

}