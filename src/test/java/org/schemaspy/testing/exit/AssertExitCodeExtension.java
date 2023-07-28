package org.schemaspy.testing.exit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class AssertExitCodeExtension implements BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback {

    private SecurityManager original;
    private final PreventSystemExitSecurityManager our = new PreventSystemExitSecurityManager();

    private boolean active = false;
    private boolean triggered = false;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        findAnnotation(context.getElement(), AssertExitCode.class).ifPresent(assertExitCode -> {
            original = System.getSecurityManager();
            System.setSecurityManager(our.reset());
            active = true;
            triggered = false;
        });
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        findAnnotation(context.getElement(), AssertExitCode.class).ifPresent(assertExitCode -> {
            our.reset();
            System.setSecurityManager(original);
        });
        if (active) {
            if (!triggered) {
                fail("Security Exception was never thrown, assertion of ExitCode never happened");
            }
            active = false;
            triggered = false;
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Optional<AssertExitCode> assertExitCodeOptional = findAnnotation(context.getElement(), AssertExitCode.class);
        if (assertExitCodeOptional.isPresent()) {
            assertThat(throwable).as("SecurityException not thrown, System.exit never called").isInstanceOf(SecurityException.class);
            assertThat(our.getExitCode()).isNotNull();
            assertThat(our.getExitCode()).as("Incorrect ExitCode").isEqualTo(assertExitCodeOptional.get().value());
            triggered = true;
        } else {
            throw throwable;
        }
    }
}
