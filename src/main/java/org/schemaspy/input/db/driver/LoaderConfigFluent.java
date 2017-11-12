package org.schemaspy.input.db.driver;

public class LoaderConfigFluent implements LoaderConfig {
    private String driverClass;
    private String driverPath;

    public LoaderConfigFluent driverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public LoaderConfigFluent driverPath(String driverPath) {
        this.driverPath = driverPath;
        return this;
    }

    @Override
    public String getDriverClass() {
        return driverClass;
    }

    @Override
    public String getDriverPath() {
        return driverPath;
    }
}
