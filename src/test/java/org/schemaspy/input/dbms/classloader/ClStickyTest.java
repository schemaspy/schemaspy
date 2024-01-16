package org.schemaspy.input.dbms.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ClStickyTest {

  @Test
  void callsOriginOnce() {
    ClassloaderSource origin = Mockito.mock(ClassloaderSource.class);
    Mockito
      .when(origin.classloader())
      .thenReturn(
        new URLClassLoader(
          new URL[0],
          this.getClass().getClassLoader()
        )
      );
    ClSticky sticky = new ClSticky(origin);
    ClassLoader cl1 = sticky.classloader();
    ClassLoader cl2 = sticky.classloader();
    ClassLoader cl3 = sticky.classloader();
    Mockito.verify(origin, Mockito.times(1)).classloader();
  }

  @Test
  void returnsSameInstance() {
    ClassloaderSource origin = Mockito.mock(ClassloaderSource.class);
    ClassLoader cl = new URLClassLoader(new URL[0]);
    ClassLoader clOther = new URLClassLoader(new URL[0]);
    Mockito
      .when(origin.classloader())
      .thenReturn(
        cl,
        clOther
      );
    ClSticky sticky = new ClSticky(origin);
    ClassLoader cl1 = sticky.classloader();
    ClassLoader cl2 = sticky.classloader();
    assertThat(cl1).isSameAs(cl2).isSameAs(cl);
  }

}