/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import java.net.URL;

/**
 * @author Nils Petzaell
 */
public interface ResourceFinder {
    URL find(String resource);
}
