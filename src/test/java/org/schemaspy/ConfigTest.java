/*
 * Copyright (C) 2017 Wojciech Kasa
 * Copyright (C) 2017 Nils Petzaell
 */
package org.schemaspy;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by wkasa on 2017-03-04.
 * @author Wojciech Kasa
 * @author Nils Petzaell
 */
public class ConfigTest {

    @Test
    public void testConfig() throws Exception {
        String[] args = {"-t", "mssql05", "-schemas", "dbo, sys", "-h"};

        Config config = new Config(args);
        assertThat(config.getSchemas().size()).isEqualTo(2);
        assertThat(config.isHelpRequired()).isTrue();
        assertThat(config.isDbHelpRequired()).isFalse();
        assertThat(config.getDbType()).isEqualToIgnoringCase("mssql05");
    }

    @Test
    public void testLoadJars() {
        Config config = new Config("-loadjars", "true");
        assertThat(config.isLoadJDBCJarsEnabled()).isTrue();
    }

    @Test
    public void testLoadProperties() {
        Config config = new Config("-configFile", "src/test/resources/configTest/loadpropertiesTest.properties");
        assertThat(config.getSchema()).isEqualToIgnoringCase("schema");
        assertThat(config.getPassword()).isEqualToIgnoringCase("database_password");
        assertThat(config.getUser()).isEqualToIgnoringCase("database_user");
        assertThat(config.getPort()).isEqualTo(123);
    }

    @Test
    public void determineDdPropertiesWillExtend() throws IOException {
        Config config = new Config();
        Map expected = new HashMap<>();
        expected.put("level", "2");
        expected.put("branch", "A");
        expected.put("level0", "zero");
        expected.put("level1", "one");
        expected.put("level2", "two");
        expected.put("avalue", "This is branch A");
        Properties dbProps = config.determineDbProperties("A2");
        assertThat(dbProps).containsAllEntriesOf(expected);
    }

    @Test
    public void determineDbPropertiesWillInclude() throws IOException {
        Config config = new Config();
        Map expected = new HashMap<>();
        expected.put("level","0");
        expected.put("branch", "B");
        expected.put("level0", "zero");
        expected.put("avalue", "This is branch A");
        Properties dbProps = config.determineDbProperties("B0");
        assertThat(dbProps).containsAllEntriesOf(expected);
    }

    @Test
    public void determineDbPropertiesNotOnClasspath() throws IOException {
        Config config = new Config();
        Map expected = new HashMap<>();
        expected.put("level","1");
        expected.put("branch", "B");
        expected.put("level0", "zero");
        expected.put("level1", "somethingelse");
        expected.put("avalue", "This is branch A");
        Properties dbProps = config.determineDbProperties("src/test/resources/dbtypes/B1");
        assertThat(dbProps).containsAllEntriesOf(expected);
    }

}