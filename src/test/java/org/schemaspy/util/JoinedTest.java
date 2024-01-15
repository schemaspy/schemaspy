package org.schemaspy.util;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class JoinedTest {

  @Test
  void wrapsOrigins() {
    Iterator iteratorA = Mockito.mock(Iterator.class);
    Iterator iteratorB = Mockito.mock(Iterator.class);
    assertThat(
        new Joined<>(() -> iteratorB, () -> iteratorB)
            .iterator()
    )
        .isNotSameAs(iteratorA)
        .isNotSameAs(iteratorB);
  }
}