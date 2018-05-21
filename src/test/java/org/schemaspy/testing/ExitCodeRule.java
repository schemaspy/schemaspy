package org.schemaspy.testing;

import org.junit.rules.ExternalResource;

import java.security.Permission;

public class ExitCodeRule extends ExternalResource {

    private SecurityManager securityManager;
    private int exitCode;

    public int getExitCode() {
        return exitCode;
    }

    @Override
    protected void before() throws Throwable {
        securityManager = System.getSecurityManager();
        System.setSecurityManager(new ExitPreventionSecurityManager());
    }

    @Override
    protected void after() {
        System.setSecurityManager(securityManager);
    }

    class ExitPreventionSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {

        }

        @Override
        public void checkPermission(Permission perm, Object context) {

        }

        @Override
        public void checkExit(int status) {
            exitCode = status;
            throw new SecurityException("Exit prevented by ExitCodeRule");
        }
    }

}
