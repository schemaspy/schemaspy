package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.schemaspy.testing.IgnoreUsingXPath;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.MySQLContainer;
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
public class MysqlXMLIT {

    private static URL expectedXML = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/test.test.xml");
    private static URL expectedDeletionOrder = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/deletionOrder.txt");
    private static URL expectedInsertionOrder = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/insertionOrder.txt");

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<MySQLContainer>(() -> new MySQLContainer<>())
            .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
            .withQueryString("?useSSL=false")
            .withInitScript("integrationTesting/dbScripts/mysqlxmlit.sql");

    @BeforeClass
    public static void generateXML() throws Exception {
        MySQLContainer container = jdbcContainerRule.getContainer();
        String[] args = new String[] {
                "-t", "mysql",
                "-db", "test",
                "-s", "test",
                "-host", container.getContainerIpAddress() + ":" + String.valueOf(container.getMappedPort(3306)),
                "-port", String.valueOf(container.getMappedPort(3306)),
                "-u", container.getUsername(),
                "-p", container.getPassword(),
                "-nohtml",
                "-o", "target/mysqlxmlit",
                "-connprops","useSSL\\=false"
        };
        Main.main(args);
    }

    @Test
    public void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/mysqlxmlit/test.test.xml"))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new IgnoreUsingXPath("/database[1]/@type")))
                .build();
        assertThat(d.getDifferences()).isEmpty();
    }

    @Test
    public void verifyDeletionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlxmlit/deletionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedDeletionOrder.openStream());
    }

    @Test
    public void verifyInsertionOrder() throws IOException {
        assertThat(Files.newInputStream(Paths.get("target/mysqlxmlit/insertionOrder.txt"), StandardOpenOption.READ)).hasSameContentAs(expectedInsertionOrder.openStream());
    }
}
