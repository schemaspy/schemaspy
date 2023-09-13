package org.schemaspy.input.dbms;

public class DbDriverLoaderErrorMessage {

    private final String[] driverClass;
    private final String driverPath;

    public DbDriverLoaderErrorMessage(
            final String []driverClass,
            final String driverPath
    ) {
        this.driverClass = driverClass;
        this.driverPath = driverPath;
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder()
                .append("Failed to create any of '")
                .append(String.join(", ", driverClass))
                .append("' driver from driverPath '")
                .append(driverPath)
                .append(".");
        return sb.toString();
    }
}
