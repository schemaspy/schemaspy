package org.schemaspy.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhenFalseTest {

  @Test
  void callsConsumerIfOriginIsFalse() {
    AtomicBoolean executed = new AtomicBoolean(false);
    new WhenFalse<>(a -> false, b -> executed.set(true)).test(new Object());
    assertThat(executed).isTrue();
  }

  @Test
  void doesNotCallConsumerIfOriginIsTrue() {
    AtomicBoolean executed = new AtomicBoolean(false);
    new WhenFalse<>(a -> true, b -> executed.set(true)).test(new Object());
    assertThat(executed).isFalse();
  }

}