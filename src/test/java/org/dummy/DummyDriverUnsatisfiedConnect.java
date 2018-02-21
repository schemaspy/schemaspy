package org.dummy;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DummyDriverUnsatisfiedConnect implements Driver {
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        throw new UnsatisfiedLinkError("I have problems with native in constructor");
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
