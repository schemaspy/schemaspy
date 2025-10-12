/*
 * Copyright (C) 2017 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schemaspy.util.ResourceNotFoundException;
import org.schemaspy.model.InvalidConfigurationException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nils Petzaell
 */
public class SimplePropertiesResolverTest {

    private static ResourceFinder resourceFinder;

    @BeforeAll
    public static void setup() {
        resourceFinder = mock(ResourceFinder.class);
        when(resourceFinder.find("A0")).thenReturn(getResource("A0"));
        when(resourceFinder.find("A1")).thenReturn(getResource("A1"));
        when(resourceFinder.find("A2")).thenReturn(getResource("A2"));
        when(resourceFinder.find("B0")).thenReturn(getResource("B0"));
        when(resourceFinder.find("XX")).thenThrow(new ResourceNotFoundException("XX"));
        when(resourceFinder.find("badInclude")).thenReturn(getResource("badInclude"));
    }

    private static URL getResource(String resource) {
        return PropertiesFinder.class.getResource("/org/schemaspy/test_types/" + resource + ".properties");
    }

    @Test
    void resolveSingleLevel() {
        SimplePropertiesResolver resolver = new SimplePropertiesResolver(resourceFinder);
        Map<String, String> expected = new HashMap<>();
        expected.put("level", "0");
        expected.put("branch", "A");
        expected.put("level0", "zero");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("A0");
        assertThat(dbProps).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void resolverWithExtends() {
        SimplePropertiesResolver resolver = new SimplePropertiesResolver(resourceFinder);
        Map<String, String> expected = new HashMap<>();
        expected.put("level", "2");
        expected.put("branch", "A");
        expected.put("level0", "zero");
        expected.put("level1", "one");
        expected.put("level2", "two");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("A2");
        assertThat(dbProps).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void resolverWillInclude() {
        SimplePropertiesResolver resolver = new SimplePropertiesResolver(resourceFinder);
        Map<String, String> expected = new HashMap<>();
        expected.put("level","0");
        expected.put("branch", "B");
        expected.put("level0", "zero");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("B0");
        assertThat(dbProps).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void noSuchPropertiesFile() {
        SimplePropertiesResolver resolver = new SimplePropertiesResolver(resourceFinder);
        assertThatThrownBy(() -> resolver.getDbProperties("XX"))
                .isInstanceOf(InvalidConfigurationException.class)
                .hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void invalidInclude() {
        SimplePropertiesResolver resolver = new SimplePropertiesResolver(resourceFinder);
        assertThatThrownBy(() -> resolver.getDbProperties("badInclude"))
                .isInstanceOf(InvalidConfigurationException.class)
                .hasMessageContaining("include.1=mysql:someRefKey");
    }
}
