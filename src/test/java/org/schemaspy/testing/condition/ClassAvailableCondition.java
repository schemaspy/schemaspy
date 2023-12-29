package org.schemaspy.testing.condition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class ClassAvailableCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return findAnnotation(
                context.getElement(),
                EnableIfClassAvailable.class
        )
                .map(this::check)
                .orElseGet(this::enabledByDefault);
    }

    private ConditionEvaluationResult check(EnableIfClassAvailable annotation) {
        String missing = Stream
                .of(annotation.value())
                .filter(this::isMissing)
                .collect(Collectors.joining(", "));
        return missing.isEmpty()
                ? enabled("All required classes are present")
                : disabled(String.format("%s are missing", missing));
    }

    private boolean isMissing(String fullClassName) {
        try {
            Class.forName(fullClassName);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    private ConditionEvaluationResult enabledByDefault() {
        String reason = String.format(
                "@%s is not present",
                EnableIfClassAvailable.class.getSimpleName()
        );
        return enabled(reason);
    }
}
