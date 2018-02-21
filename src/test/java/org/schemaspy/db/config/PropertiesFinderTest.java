/*
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy.db.config;

import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class PropertiesFinderTest {

    private PropertiesFinder propertiesFinder = new PropertiesFinder();

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

    @Test(expected = ResourceNotFoundException.class)
    public void noSuchResource() {
        propertiesFinder.find("doesNotExist");
    }

    @Test
    public void shouldNotReturnAFolderFromClassPath() throws URISyntaxException {
        URL url = propertiesFinder.find("folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }

    @Test
    public void shouldNotReturnAFolderFromPath() throws URISyntaxException {
        URL url = propertiesFinder.find("src/test/resources/org/schemaspy/types/folder");
        assertThat(Paths.get(url.toURI()).toFile().isDirectory()).isFalse();
    }
}
