package org.schemaspy.connection;

import java.io.IOException;
import java.util.Properties;

public final class WithUser implements Connection {

    private final String user;
    private final Connection origin;

    public WithUser(final String user, final Connection origin) {
        this.user = user;
        this.origin = origin;
    }

    @Override
    public Properties properties() throws IOException {
        final Properties result = this.origin.properties();
        if (user != null) {
            result.put("user", user);
        }
        return result;
    }
}
