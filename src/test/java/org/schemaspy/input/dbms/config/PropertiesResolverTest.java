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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.schemaspy.input.dbms.exceptions.ResourceNotFoundException;
import org.schemaspy.model.InvalidConfigurationException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nils Petzaell
 */
public class PropertiesResolverTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static ResourceFinder resourceFinder;

    @BeforeClass
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
        return PropertiesFinder.class.getResource("/org/schemaspy/types/" + resource + ".properties");
    }

    @Test
    public void resolveSingleLevel() {
        PropertiesResolver resolver = new PropertiesResolver(resourceFinder);
        Map expected = new HashMap<>();
        expected.put("level", "0");
        expected.put("branch", "A");
        expected.put("level0", "zero");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("A0");
        assertThat(dbProps.entrySet()).containsExactlyInAnyOrder(toArray(expected));
    }

    @Test
    public void resolverWithExtends() throws IOException {
        PropertiesResolver resolver = new PropertiesResolver(resourceFinder);
        Map expected = new HashMap<>();
        expected.put("level", "2");
        expected.put("branch", "A");
        expected.put("level0", "zero");
        expected.put("level1", "one");
        expected.put("level2", "two");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("A2");
        assertThat(dbProps.entrySet()).containsExactlyInAnyOrder(toArray(expected));
    }

    @Test
    public void resolverWillInclude() throws IOException {
        PropertiesResolver resolver = new PropertiesResolver(resourceFinder);
        Map expected = new HashMap<>();
        expected.put("level","0");
        expected.put("branch", "B");
        expected.put("level0", "zero");
        expected.put("avalue", "This is branch A");
        Properties dbProps = resolver.getDbProperties("B0");
        assertThat(dbProps.entrySet()).containsExactlyInAnyOrder(toArray(expected));
    }

    private Map.Entry[] toArray(Map map) {
        return (Map.Entry[])map.entrySet().toArray(new Map.Entry[map.size()]);
    }

    @Test
    public void noSuchPropertiesFile() {
        thrown.expect(InvalidConfigurationException.class);
        thrown.expectCause(instanceOf(ResourceNotFoundException.class));
        PropertiesResolver resolver = new PropertiesResolver(resourceFinder);
        resolver.getDbProperties("XX");
    }

    @Test
    public void invalidInclude() {
        thrown.expect(InvalidConfigurationException.class);
        thrown.expectMessage("include.1=mysql:someRefKey");
        PropertiesResolver resolver = new PropertiesResolver(resourceFinder);
        resolver.getDbProperties("badInclude");
    }
}
