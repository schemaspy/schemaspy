package org.schemaspy.testing.condition;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ClassAvailableCondition.class)
public @interface EnableIfClassAvailable {
    /**
     * Array of fully qualified class names that
     * controls if this test is enabled.
     * @return classes that need to be available.
     */
    String[] value();
}
