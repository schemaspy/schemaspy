package org.schemaspy.input.dbms;

public class DbDriverLoaderErrorMessage {

    private final String[] driverClass;

    public DbDriverLoaderErrorMessage(
            final String []driverClass
    ) {
        this.driverClass = driverClass;
    }

    public String createMessage() {
        return String.format(
            "Failed to create any of '%s' driver from driver path.",
            String.join(", ", driverClass)
        );
    }
}
