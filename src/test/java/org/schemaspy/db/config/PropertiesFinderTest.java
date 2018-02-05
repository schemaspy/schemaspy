/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import org.junit.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class PropertiesFinderTest {

    private PropertiesFinder propertiesFinder = new PropertiesFinder();

    @Test
    public void findOnClassPathWithoutExtension() {
        URL url = propertiesFinder.find("E0");
        assertThat(url).isNotNull();
    }

    @Test
    public void findOnClassPathWithExtension() {
        URL url = propertiesFinder.find("A0.properties");
        assertThat(url).isNotNull();
    }
    @Test
    public void findOnClassPathWithNoExtension() {
        URL url = propertiesFinder.find("A0");
        assertThat(url).isNotNull();
    }

    @Test
    public void findByPathWithOutExtension() {
        URL url = propertiesFinder.find("src/test/resources/dbtypes/C0");
        assertThat(url).isNotNull();
    }

    @Test
    public void findByPathWithExtension() {
        URL url = propertiesFinder.find("src/test/resources/dbtypes/C0.properties");
        assertThat(url).isNotNull();
    }

    @Test
    public void findByPathHasNoExtension() {
        URL url = propertiesFinder.find("src/test/resources/dbtypes/D0");
        assertThat(url).isNotNull();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void noSuchResource() {
        propertiesFinder.find("doesNotExist");
    }
}
