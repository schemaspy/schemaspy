package org.schemaspy.output.dot.schemaspy.node.footer;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;

class ChildrenTest {

  @ParameterizedTest(name = "{index} ==> When show:{1} and count:{0} then output: {2}")
  @CsvSource({
      "0, true, '0 &gt;'",
      "0, false, '  '",
      "10, true, '10 &gt;'",
      "10, false, '10 &gt;'"
  })
  void children(int count, boolean show, String expected) {
    assertThat(
        new Children(count, show).value(),
        new IsEqual<>( expected)
    );
  }

}