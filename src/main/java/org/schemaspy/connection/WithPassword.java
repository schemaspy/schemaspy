package org.schemaspy.connection;

import org.schemaspy.Config;

import java.io.IOException;
import java.util.Properties;

public class WithPassword implements Connection {

    private final String password;
    private final Connection origin;

    public WithPassword(final Config config, final Connection origin) {
        this(config.getPassword(), origin);
    }

    public WithPassword(final String password, final Connection origin) {
        this.password = password;
        this.origin = origin;
    }

    @Override
    public Properties properties() throws IOException {
        final Properties result = this.origin.properties();
        if (this.password != null) {
            result.put("password", this.password);
        }
        return result;
    }
}
