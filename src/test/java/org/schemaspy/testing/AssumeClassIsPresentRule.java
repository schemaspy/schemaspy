package org.schemaspy.testing;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class AssumeClassIsPresentRule implements TestRule {

    private final String className;

    public AssumeClassIsPresentRule(String className) {
        this.className = className;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssumptionViolatedException("Class not present " + className);
        }
        return base;
    }
}
