package org.schemaspy.output.dot.schemaspy.node.footer;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;

class RowsTest {

  @ParameterizedTest(name = "{index} ==> When enabled:{1} and count: {0} output will be: {2}")
  @CsvSource({
      "0, true, '0 rows'",
      "0, false, '  '",
      "1, true, '1 row'",
      "1, false, '  '",
      "10, true, '10 rows'",
      "10, false, '  '"
  })
  void rows(long count, boolean enabled, String expected) {
    assertThat(
        new Rows(count, enabled).value(),
        new IsEqual<>(expected)
    );
  }
}