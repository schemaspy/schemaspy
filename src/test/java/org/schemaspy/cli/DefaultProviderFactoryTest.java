package org.schemaspy.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultProviderFactoryTest {

    @Test
    void canCreateWithoutSpecifiedAndDefault() {
        assertThat(
                new DefaultProviderFactory(Optional.empty())
                        .defaultProvider()
        )
                .isNotNull();
    }

    @Test
    void usesProvidedPropertiesFile() {
        assertThat(
                new DefaultProviderFactory(
                        Optional.of(
                                Paths.get("src", "test", "resources", "cli", "defaultProperties.properties").toString()
                        )
                )
                        .defaultProvider()
                        .getDefaultValueFor("defaultProperties")
        )
                .isEqualTo("it has been loaded");
    }

    @Test
    void incorrectPropertiesThrowsException() {
        DefaultProviderFactory defaultProviderFactory = new DefaultProviderFactory(Optional.of("doesnt exist"));
        assertThatThrownBy(() -> defaultProviderFactory.defaultProvider());
    }

    @Test
    void fallBackToDefault() {
        assertThat(
                new DefaultProviderFactory(
                        Optional.empty(),
                        Paths.get("src", "test", "resources", "cli", "defaultProperties.properties").toString()
                )
                        .defaultProvider()
                        .getDefaultValueFor("defaultProperties")
        )
                .isEqualTo("it has been loaded");
    }

}