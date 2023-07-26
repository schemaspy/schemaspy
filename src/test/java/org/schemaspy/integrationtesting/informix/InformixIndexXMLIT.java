/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.integrationtesting.informix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.schemaspy.Main;
import org.schemaspy.testing.IgnoreNonPrintedInCData;
import org.schemaspy.testing.IgnoreUsingXPath;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.InformixContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.schemaspy.testing.SchemaSpyRunnerFixture.schemaSpyRunner;

/**
 * @author Nils Petzaell
 */
@DisabledOnOs(value = OS.MAC, architectures = {"aarch64"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
public class InformixIndexXMLIT {

    private static final URL expectedXML = InformixIndexXMLIT.class.getResource("/integrationTesting/informix/expecting/test.informix.xml");
    private static final URL expectedDeletionOrder = InformixIndexXMLIT.class.getResource("/integrationTesting/informix/expecting/deletionOrder.txt");
    private static final URL expectedInsertionOrder = InformixIndexXMLIT.class.getResource("/integrationTesting/informix/expecting/insertionOrder.txt");

    @Container
    public static InformixContainer informixContainer =
            new InformixContainer()
                    .withInitScript("integrationTesting/informix/dbScripts/informix.sql");

    private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

    @BeforeEach
    public void createXML() {
        if (shouldRun.get()) {
            String[] args = {
                    "-t", "informix",
                    "-db", "test",
                    "-s", "informix",
                    "-cat", "test",
                    "-server", "dev",
                    "-o", "target/testout/integrationtesting/informix/xml",
                    "-u", informixContainer.getUsername(),
                    "-p", informixContainer.getPassword(),
                    "-host", informixContainer.getHost(),
                    "-port", informixContainer.getJdbcPort().toString(),
                    "-nohtml"
            };
            schemaSpyRunner(args).run();
            shouldRun.set(false);
        }
    }

    @Test
    void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/testout/integrationtesting/informix/xml/test.informix.xml"))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new IgnoreUsingXPath("/database[1]/@type"), new IgnoreNonPrintedInCData()))
                .build();
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/informix/xml/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/testout/integrationtesting/informix/xml/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }

}
