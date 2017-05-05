/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011 John Currier
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy;

import org.schemaspy.model.ConnectionFailure;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.model.InvalidConfigurationException;
import org.schemaspy.model.ProcessExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author John Currier
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Autowired
    private SchemaAnalyzer analyzer;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int rc = 1;

        try {
            rc = analyzer.analyze(new Config(args)) == null ? 1 : 0;
        } catch (ConnectionFailure couldntConnect) {
            LOGGER.log(Level.WARNING, "Connection Failure", couldntConnect);
            rc = 3;
        } catch (EmptySchemaException noData) {
            LOGGER.log(Level.WARNING, "Empty schema", noData);
            rc = 2;
        } catch (InvalidConfigurationException badConfig) {
            LOGGER.info("");
            if (badConfig.getParamName() != null)
                LOGGER.log(Level.WARNING, "Bad parameter specified for " + badConfig.getParamName());
            LOGGER.log(Level.WARNING, "Bad config " + badConfig.getMessage());
            if (badConfig.getCause() != null && !badConfig.getMessage().endsWith(badConfig.getMessage()))
                LOGGER.log(Level.WARNING, " caused by " + badConfig.getCause().getMessage());

            LOGGER.log(Level.FINE, "Command line parameters: " + Arrays.asList(args));
            LOGGER.log(Level.FINE, "Invalid configuration detected", badConfig);
        } catch (ProcessExecutionException badLaunch) {
            LOGGER.log(Level.WARNING, badLaunch.getMessage(), badLaunch);
        } catch (Exception exc) {
            LOGGER.log(Level.SEVERE, exc.getMessage(), exc);
        }

        System.exit(rc);
    }
}