/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

/**
 * @author Nils Petzaell
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
