/*
 * Copyright (C) 2025 SchemaSpy Contributors
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
package org.schemaspy.integrationtesting;

import org.junit.platform.suite.api.*;
import org.schemaspy.testing.SQLScriptsRunner;
import org.schemaspy.testing.testcontainers.BigQueryContainer;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;
import org.testcontainers.utility.DockerImageName;

/**
 * Test suite for BigQuery integration tests using Google Cloud BigQuery Emulator.
 *
 * <p><strong>Platform Compatibility Note:</strong></p>
 * <p>The BigQuery emulator currently only supports AMD64/x86_64 architecture.
 * If running on ARM64 (Apple Silicon M1/M2/M3), these tests will be skipped
 * unless you have Rosetta 2 emulation enabled in Docker Desktop settings.</p>
 *
 * <p>To enable on Apple Silicon:</p>
 * <ol>
 * <li>Open Docker Desktop settings</li>
 * <li>Go to "Features in development"</li>
 * <li>Enable "Use Rosetta for x86/amd64 emulation on Apple Silicon"</li>
 * <li>Restart Docker Desktop</li>
 * </ol>
 *
 * <p><strong>CI/CD Compatibility:</strong></p>
 * <p>These tests run successfully in CI/CD environments using AMD64/x86_64 runners
 * (e.g., GitHub Actions with ubuntu-latest). The Suite pattern ensures tests are
 * executed during the Maven verify phase via failsafe plugin.</p>
 *
 * @see <a href="https://testcontainers.com/modules/gcloud/">TestContainers GCloud Module</a>
 */
@Suite
@SuiteDisplayName("BigQuery Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.bigquery")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class BigQuerySuite {

    /**
     * BigQuery Emulator container configuration.
     * 
     * The BigQuery emulator provides a local BigQuery instance for testing without
     * requiring actual GCP credentials or incurring cloud costs.
     * 
     * Note: The emulator has some limitations compared to real BigQuery:
     * - Limited SQL feature support
     * - No IAM or authentication
     * - Simplified execution model
     * - AMD64/x86_64 architecture only (use Rosetta 2 on Apple Silicon)
     */
    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new BigQueryContainer(
                            DockerImageName.parse("ghcr.io/goccy/bigquery-emulator:latest")
                                    .asCompatibleSubstituteFor("bigquery-emulator"),
                            "test-project"
                    )
            )
            .withInitFunctions(new SQLScriptsRunner("integrationTesting/bigquery/dbScripts/"));

}
