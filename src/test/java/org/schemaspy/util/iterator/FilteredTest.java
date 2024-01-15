package org.schemaspy.util.iterator;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilteredTest {

  @Test
  void willFilter() {
    assertThat(
        new Filtered<>(List.of("one", "two").iterator(), "one"::equals)
    ).toIterable()
        .containsExactly("one");
  }

  @Test
  void emptyIterator() {
    assertThat(
        new Filtered<String>(
            Collections.emptyIterator(),
            Predicate.not(String::isEmpty)
        )
    ).isExhausted();
  }

  @Test
  void throwsNoSuchElementException() {
    Filtered<String> filtered =
        new Filtered<>(
            Collections.emptyIterator(),
            Predicate.not(String::isEmpty)
        );
    assertThatThrownBy(
        () -> filtered.next()
    ).isInstanceOf(NoSuchElementException.class);
  }


}