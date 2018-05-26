/*
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.db.config;

import org.schemaspy.db.exceptions.RuntimeIOException;
import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class PropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ResourceFinder resourceFinder;

    public PropertiesResolver() {
        resourceFinder = new PropertiesFinder();
    }

    public PropertiesResolver(ResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }

    public Properties getDbProperties(String dbType) {
        try {
            ResolutionInfo resolutionInfo = new ResolutionInfo(dbType);
            URL url = resourceFinder.find(dbType);
            resolutionInfo.addTrace(url);
            Properties props = fromURL(url);
            processIncludes(props, resolutionInfo);
            processExtends(props, resolutionInfo);
            LOGGER.debug(resolutionInfo.getTrace());
            return props;
        } catch (ResourceNotFoundException rnfe) {
            throw new InvalidConfigurationException("Unable to resolve databaseType: " + dbType, rnfe)
                    .setParamName("-t")
                    .setParamValue(dbType);
        }
    }

    private Properties fromURL(URL url) {
        Properties properties = new Properties();
        try {
            try (InputStream inputStream = url.openStream()) {
                properties.load(inputStream);
            }
            } catch (IOException e) {
               throw new RuntimeIOException("Failed to load properties from: " + url.getFile(), e);
        }
        return properties;
    }

    private void processIncludes(Properties props, ResolutionInfo resolutionInfo) {
        Map<String,String> includes = props.entrySet().stream()
                .filter(isInclude)
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
        includes.keySet().forEach(props::remove);
        includes.values().stream()
                .map( s -> s.split("::"))
                .forEach( ref -> {
                    Properties refProps = resolve(ref[0], resolutionInfo);
                    props.putIfAbsent(ref[1], refProps.getProperty(ref[1]));
                });

    }

    private static final Predicate<Map.Entry> isInclude = e -> {
        if (e.getKey().toString().startsWith("include.")) {
            if (e.getValue().toString().split("::").length == 2) {
                return true;
            }
            String msg = "Include directive is incorrect {" +
                    e.getKey().toString() +
                    "=" +
                    e.getValue().toString() + "}";
            throw new InvalidConfigurationException(msg);
        } else {
            return false;
        }
    };

    private void processExtends(Properties props, ResolutionInfo resolutionInfo) {
        String parentDbType = (String)props.remove("extends");
        Properties parentProperties;
        if (Objects.nonNull(parentDbType)) {
            parentProperties = resolve(parentDbType.trim(), resolutionInfo);
            parentProperties.forEach(props::putIfAbsent);
        }
    }

    private Properties resolve(String dbType, ResolutionInfo resolutionInfo) {
        URL url = resourceFinder.find(dbType);
        resolutionInfo.addTrace(url);
        Properties props = fromURL(url);
        processIncludes(props, resolutionInfo);
        processExtends(props, resolutionInfo);
        return props;
    }
}
