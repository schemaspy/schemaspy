package org.schemaspy.testing.exit;

import java.security.Permission;

public class PreventSystemExitSecurityManager extends SecurityManager {

    private Integer exitCode = null;

    @Override
    public void checkPermission(Permission perm) {
        // Mock object. Do nothing.
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        // Mock object. Do nothing.
    }

    @Override
    public void checkExit(int status) {
        exitCode = status;
        throw new SecurityException("Exit prevented by ExitCodeRule");
    }

    public PreventSystemExitSecurityManager reset() {
        exitCode = null;
        return this;
    }

    public Integer getExitCode() {
        return exitCode;
    }
}
