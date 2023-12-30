package org.schemaspy.input.dbms.service.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Formats elements to avoid near-identical duplicates.
 */
public class UniformSet {

    private final String[] elements;

    public UniformSet(String[] elements) {
        this.elements = elements;
    }

    public Set<String> value() {
        return Collections.unmodifiableSet(Arrays.stream(this.elements)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toUpperCase)
            .collect(Collectors.toSet()));
    }
}
