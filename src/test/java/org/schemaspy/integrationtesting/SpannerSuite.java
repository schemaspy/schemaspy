/*
 * Copyright (C) 2024 Nils Petzaell
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
import org.schemaspy.testing.testcontainers.SpannerContainer;
import org.schemaspy.testing.testcontainers.SuiteContainerExtension;
import org.testcontainers.utility.DockerImageName;

/**
 * Test suite for Google Cloud Spanner integration tests using the Spanner emulator.
 *
 * <p><strong>Platform Compatibility Note:</strong></p>
 * <p>The Spanner emulator supports multiple architectures including AMD64/x86_64 and ARM64.
 * These tests should work on most development machines and CI/CD environments.</p>
 *
 * <p><strong>CI/CD Compatibility:</strong></p>
 * <p>These tests run successfully in CI/CD environments using AMD64/x86_64 or ARM64 runners
 * (e.g., GitHub Actions with ubuntu-latest). The Suite pattern ensures tests are
 * executed during the Maven verify phase via failsafe plugin.</p>
 *
 * @see <a href="https://cloud.google.com/spanner/docs/emulator">Cloud Spanner Emulator</a>
 */
@Suite
@SuiteDisplayName("Spanner Test Suite")
@SelectPackages("org.schemaspy.integrationtesting.spanner")
@IncludeClassNamePatterns(".*IT$")
@IncludeEngines("junit-jupiter")
public class SpannerSuite {

    /**
     * Google Cloud Spanner Emulator container configuration.
     *
     * The Spanner emulator provides a local Spanner instance for testing without
     * requiring actual GCP credentials or incurring cloud costs.
     *
     * Features:
     * - Full DDL support (CREATE TABLE, indexes, foreign keys)
     * - DML support (INSERT, UPDATE, DELETE, SELECT)
     * - Transaction support
     * - Schema information queries
     * - Multi-architecture support (AMD64 and ARM64)
     *
     * Limitations compared to real Spanner:
     * - Single-region only
     * - No replication
     * - Performance characteristics differ
     * - Some advanced features may be limited
     */
    public static final SuiteContainerExtension SUITE_CONTAINER =
            new SuiteContainerExtension(
                    () -> new SpannerContainer(
                            DockerImageName.parse("gcr.io/cloud-spanner-emulator/emulator:latest"),
                            "test-project",
                            "test-instance",
                            "test-database"
                    )
            )
            .withInitFunctions(new SQLScriptsRunner("integrationTesting/spanner/dbScripts/"));

}
