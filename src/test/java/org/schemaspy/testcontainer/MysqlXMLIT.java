package org.schemaspy.testcontainer;

import com.github.npetzall.testcontainers.junit.jdbc.JdbcContainerRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.schemaspy.SchemaAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MySQLContainer;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.github.npetzall.testcontainers.junit.jdbc.JdbcAssumptions.assumeDriverIsPresent;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MysqlXMLIT {

    private static URL expectedXML = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/test.test.xml");
    private static URL expectedDeletionOrder = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/deletionOrder.txt");
    private static URL expectedInsertionOrder = MysqlXMLIT.class.getResource("/integrationTesting/expecting/mysqlxmlit/insertionOrder.txt");

    @ClassRule
    public static JdbcContainerRule<MySQLContainer> jdbcContainerRule =
            new JdbcContainerRule<MySQLContainer>(() -> new MySQLContainer<>("mysql:5.7.18"))
            .assumeDockerIsPresent().withAssumptions(assumeDriverIsPresent())
            .withQueryString("?useSSL=false")
            .withInitScript("integrationTesting/dbScripts/mysqlxmlit.sql");

    @Configuration
    @ComponentScan(basePackages = {"org.schemaspy"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = Main.class))
    static class MysqlXMLITConfig {
        @Bean
        public ApplicationArguments applicationArguments() {
            MySQLContainer container = jdbcContainerRule.getContainer();
            return new DefaultApplicationArguments(new String[]{
                    "-t", "mysql",
                    "-db", "test",
                    "-s", "test",
                    "-host", container.getContainerIpAddress() + ":" + String.valueOf(container.getMappedPort(3306)),
                    "-port", String.valueOf(container.getMappedPort(3306)),
                    "-u", container.getUsername(),
                    "-p", container.getPassword(),
                    "-nohtml",
                    "-o", "target/mysqlxmlit",
                    "-connprops", "useSSL\\=false"
            });
        }
    }

    @Autowired
    private SchemaAnalyzer analyzer;

    private static volatile boolean hasRun = false;

    @Before
    public synchronized void generateXML() throws Exception {
        if (!hasRun) {
            analyzer.analyze();
            hasRun = true;
        }
    }

    @Test
    public void verifyXML() {
        Diff d = DiffBuilder.compare(Input.fromURL(expectedXML))
                .withTest(Input.fromFile("target/mysqlxmlit/test.test.xml"))
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
