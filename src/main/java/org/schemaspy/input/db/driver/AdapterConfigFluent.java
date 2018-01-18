package org.schemaspy.input.db.driver;

import java.util.Properties;

public class AdapterConfigFluent implements AdapterConfig {

    private String user;
    private String password;

    private Properties connectionProps = new Properties();

    private String connectionUrl;

    @Override
    public boolean hasUser() {
        return user != null && !user.isEmpty();
    }

    @Override
    public String getUser() {
        return user;
    }

    public AdapterConfigFluent user(String user) {
        this.user = user;
        return this;
    }

    @Override
    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    @Override
    public String getPassword() {
        return password;
    }

    public AdapterConfigFluent password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public Properties getConnectionProps() {
        return connectionProps;
    }

    public AdapterConfigFluent connectionProps(Properties connectionProps) {
        this.connectionProps = connectionProps;
        return this;
    }

    public AdapterConfigFluent addConnectionProps(Properties connectionProps) {
        this.connectionProps.putAll(connectionProps);
        return this;
    }

    public AdapterConfigFluent addConnectionProp(String key, String value) {
        this.connectionProps.setProperty(key, value);
        return this;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public AdapterConfigFluent connectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }
}
