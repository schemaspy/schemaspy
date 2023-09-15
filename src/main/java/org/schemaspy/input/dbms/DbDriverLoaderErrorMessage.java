package org.schemaspy.input.dbms;

public class DbDriverLoaderErrorMessage {

    private final String[] driverClass;

    public DbDriverLoaderErrorMessage(
            final String []driverClass
    ) {
        this.driverClass = driverClass;
    }

    public String createMessage() {
        StringBuilder sb = new StringBuilder()
                .append("Failed to create any of '")
                .append(String.join(", ", driverClass))
                .append("' driver from driver path.");
        return sb.toString();
    }
}
