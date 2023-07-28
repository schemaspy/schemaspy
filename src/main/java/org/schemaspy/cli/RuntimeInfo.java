package org.schemaspy.cli;

public class RuntimeInfo {

    private final String applicationName;
    private final String applicationVersion;

    public RuntimeInfo(String applicationName, String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public String toString() {
        return String.format(
                "Running %s %s with Java(%s) %s PID %s [%s, %s, %s, %s] started by %s in %s",
                applicationName,
                applicationVersion,
                System.getProperty("java.vendor"),
                System.getProperty("java.version"),
                ProcessHandle.current().pid(),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                new SchemaSpyJarFile().path().toString(),
                System.getProperty("user.name"),
                System.getProperty("user.dir")
        );
    }
}
