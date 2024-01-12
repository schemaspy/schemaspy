package org.schemaspy.util;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class MappedTest {

  @Test
  void wrapsOrigin() {
    Iterator<?> iterator = Mockito.mock(Iterator.class);
    assertThat(
        new Mapped<>(() -> iterator, b -> b)
            .iterator()
    ).isNotSameAs(iterator);
  }

}