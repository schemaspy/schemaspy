package org.schemaspy.testing;

import org.junit.rules.ExternalResource;
import org.schemaspy.testing.exit.PreventSystemExitSecurityManager;

public class ExitCodeRule extends ExternalResource {

    private SecurityManager original;
    private PreventSystemExitSecurityManager our = new PreventSystemExitSecurityManager();

    public Integer getExitCode() {
        return our.getExitCode();
    }

    @Override
    protected void before() throws Throwable {
        original = System.getSecurityManager();
        System.setSecurityManager(our.reset());
    }

    @Override
    protected void after() {
        our.reset();
        System.setSecurityManager(original);
    }

}
