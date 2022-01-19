package org.schemaspy.testing;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.schemaspy.Config;

public class ConfigExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext extensionContext) {
        Config.setInstance(null);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        Config.setInstance(null);
    }
}
