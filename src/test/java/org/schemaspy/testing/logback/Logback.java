package org.schemaspy.testing.logback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logback {
    Class<?> value() default Void.class;
    String logger() default "";
    String level() default "INFO";
    String pattern() default "%m";
}
