package org.schemaspy.util;

import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link WhenIf}.
 */
class WhenIfTest {

  @Test
  void actOnFalse() {
    AtomicBoolean onTrue = new AtomicBoolean(false);
    AtomicBoolean onFalse = new AtomicBoolean(false);
    new WhenIf<>(
        a -> false,
        b -> onTrue.set(true),
        c -> onFalse.set(true)
    ).test(new Object());
    assertThat(onTrue).isFalse();
    assertThat(onFalse).isTrue();
  }

  @Test
  void actOnTrue() {
    AtomicBoolean onTrue = new AtomicBoolean(false);
    AtomicBoolean onFalse = new AtomicBoolean(false);
    new WhenIf<>(
        a -> true,
        b -> onTrue.set(true),
        c -> onFalse.set(true)
    ).test(new Object());
    assertThat(onTrue).isTrue();
    assertThat(onFalse).isFalse();
  }
}
