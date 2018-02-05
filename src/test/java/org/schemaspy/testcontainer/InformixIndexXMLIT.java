package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.schemaspy.testing.AssumeClassIsPresentRule;
import org.schemaspy.testing.IgnoreUsingXPath;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.InformixContainer;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class InformixIndexXMLIT {

    private static URL expectedXML = InformixIndexXMLIT.class.getResource("/integrationTesting/informixIndexXMLIT/expecting/test.informix.xml");
    private static URL expectedDeletionOrder = InformixIndexXMLIT.class.getResource("/integrationTesting/informixIndexXMLIT/expecting/deletionOrder.txt");
    private static URL expectedInsertionOrder = InformixIndexXMLIT.class.getResource("/integrationTesting/informixIndexXMLIT/expecting/insertionOrder.txt");

    public static TestRule jdbcDriverClassPresentRule = new AssumeClassIsPresentRule("com.informix.jdbc.IfxDriver");

    public static JdbcContainerRule<InformixContainer> jdbcContainerRule =
            new JdbcContainerRule<>(() -> new InformixContainer())
                    .assumeDockerIsPresent()
                    .withAssumptions(assumeDriverIsPresent())
                    .withInitScript("integrationTesting/informixIndexXMLIT/dbScripts/informix.sql");

    @ClassRule
    public static final TestRule chain = RuleChain
            .outerRule(jdbcContainerRule)
            .around(jdbcDriverClassPresentRule);

    @BeforeClass
    public static void createXML() throws Exception {
        String[] args = {
                "-t", "informix",
                "-db", "test",
                "-s", "informix",
                "-cat", "test",
                "-server", "dev",
                "-o", "target/informix_xml",
                "-u", jdbcContainerRule.getContainer().getUsername(),
                "-p", jdbcContainerRule.getContainer().getPassword(),
                "-host", jdbcContainerRule.getContainer().getContainerIpAddress(),
                "-port", jdbcContainerRule.getContainer().getJdbcPort().toString(),
                "-nohtml"
        };
        Main.main(args);
    }

    @Test
    public void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/informix_xml/test.informix.xml"))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new IgnoreUsingXPath("/database[1]/@type")))
                .build();
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/informix_xml/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/informix_xml/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }

}
