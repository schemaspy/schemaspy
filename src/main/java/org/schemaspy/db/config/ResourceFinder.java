package org.schemaspy.db.config;

import java.net.URL;

public interface ResourceFinder {
    URL find(String resource);
}
