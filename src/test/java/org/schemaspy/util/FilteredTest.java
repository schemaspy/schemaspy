package org.schemaspy.util;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class FilteredTest {

  @Test
  void wrapsIterator() {
    Iterator<?> iterator = Mockito.mock(Iterator.class);
    assertThat(
        new Filtered(() -> iterator, (t) -> true)
            .iterator()
    ).isNotSameAs(iterator);
  }
}