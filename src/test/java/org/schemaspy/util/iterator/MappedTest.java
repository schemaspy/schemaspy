package org.schemaspy.util.iterator;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappedTest {

  @Test
  void willMap() {
    assertThat(
        new Mapped<>(
            List.of("one", "two")
                .iterator(),
            String::toUpperCase
        )
    ).toIterable()
        .containsExactly("ONE", "TWO");
  }

  @Test
  void emptyIterator() {
    assertThat(
        new Mapped<String,String>(
            Collections.emptyIterator(),
            String::toUpperCase
        )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    Mapped<String,String> mapped =
        new Mapped<>(
            Collections.emptyIterator(),
            String::toUpperCase
        );
    assertThatThrownBy(
        () -> mapped.next()
    ).isInstanceOf(NoSuchElementException.class);
  }

}