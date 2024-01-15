package org.schemaspy.util.iterator;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinedTest {

  @Test
  void join() {
    assertThat(
        new Joined<>(
            List.of("one", "two").iterator(),
            List.of("three", "four").iterator()
        )
    ).toIterable().containsExactly("one", "two", "three", "four");
  }

  @Test
  void emptyIterator() {
    assertThat(new Joined<>(
        Collections.emptyIterator()
    )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    Joined<String> joined =
        new Joined(
            Collections.emptyIterator()
        );
    assertThatThrownBy(
        () -> joined.next()
    ).isInstanceOf(NoSuchElementException.class);
  }

}