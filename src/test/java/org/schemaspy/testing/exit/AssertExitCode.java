package org.schemaspy.testing.exit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AssertExitCodeExtension.class)
public @interface AssertExitCode {
    int value() default 0;
}
