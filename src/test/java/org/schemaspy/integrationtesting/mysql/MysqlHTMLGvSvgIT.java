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
package org.schemaspy.integrationtesting.mysql;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.schemaspy.cli.SchemaSpyRunner;
import org.schemaspy.integrationtesting.MysqlSuite;
import org.schemaspy.testing.HtmlOutputValidator;
import org.schemaspy.testing.SuiteOrTestJdbcContainerRule;
import org.schemaspy.testing.XmlOutputDiff;
import org.testcontainers.containers.MySQLContainer;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;

/**
 * @author Nils Petzaell
 */
public class MysqlHTMLGvSvgIT {

	private static final Path outputPath = Paths.get("target", "testout", "integrationtesting", "mysql", "html_gv_svg");

	private static URL expectedXML = MysqlHTMLGvSvgIT.class
			.getResource("/integrationTesting/mysql/expecting/mysqlhtml_gv_svg/htmlit.htmlit.xml");
	private static URL expectedDeletionOrder = MysqlHTMLGvSvgIT.class
			.getResource("/integrationTesting/mysql/expecting/mysqlhtml_gv_svg/deletionOrder.txt");
	private static URL expectedInsertionOrder = MysqlHTMLGvSvgIT.class
			.getResource("/integrationTesting/mysql/expecting/mysqlhtml_gv_svg/insertionOrder.txt");

	@SuppressWarnings("unchecked")
	@ClassRule
	public static JdbcContainerRule<MySQLContainer<?>> jdbcContainerRule = new SuiteOrTestJdbcContainerRule<MySQLContainer<?>>(
			MysqlSuite.jdbcContainerRule,
			new JdbcContainerRule<MySQLContainer<?>>(() -> new MySQLContainer<>("mysql:5")).assumeDockerIsPresent()
					.withAssumptions(assumeDriverIsPresent()).withQueryString("?useSSL=false")
					.withInitScript("integrationTesting/mysql/dbScripts/htmlit.sql").withInitUser("root", "test"));

	private SchemaSpyRunner schemaSpyRunner = new SchemaSpyRunner();

	private static final AtomicBoolean shouldRun = new AtomicBoolean(true);

	@Before
	public synchronized void generateHTML() throws Exception {
		if (shouldRun.get()) {
			String[] args = new String[] { "-t", "mysql", "-db", "htmlit", "-s", "htmlit", "-imageformat", "svg",
					"-host",
					jdbcContainerRule.getContainer().getContainerIpAddress() + ":"
							+ jdbcContainerRule.getContainer().getMappedPort(3306),
					"-port", String.valueOf(jdbcContainerRule.getContainer().getMappedPort(3306)), "-u",
					jdbcContainerRule.getContainer().getUsername(), "-p",
					jdbcContainerRule.getContainer().getPassword(), "-o", outputPath.toString(), "-connprops",
					"useSSL\\=false" };
			schemaSpyRunner.run(args);
			shouldRun.set(false);
		}
	}

	@Test
	public void verifyXML() {
		Diff d = XmlOutputDiff.diffXmlOutput(Input.fromFile(outputPath.resolve("htmlit.htmlit.xml").toString()),
				Input.fromURL(expectedXML));
		assertThat(d.getDifferences()).isEmpty();
	}

	@Test
	public void verifyDeletionOrder() throws IOException {
		assertThat(Files.newInputStream(outputPath.resolve("deletionOrder.txt"), StandardOpenOption.READ))
				.hasSameContentAs(expectedDeletionOrder.openStream());
	}

	@Test
	public void verifyInsertionOrder() throws IOException {
		assertThat(Files.newInputStream(outputPath.resolve("insertionOrder.txt"), StandardOpenOption.READ))
				.hasSameContentAs(expectedInsertionOrder.openStream());
	}

	@Test
	public void producesSameContent() throws IOException {
		SoftAssertions softAssertions = HtmlOutputValidator.hasProducedValidOutput(outputPath,
				Paths.get("src", "test", "resources", "integrationTesting", "mysql", "expecting", "mysqlhtml_gv_svg"));
		softAssertions.assertThat(softAssertions.wasSuccess()).isTrue();
		softAssertions.assertAll();
	}
}
