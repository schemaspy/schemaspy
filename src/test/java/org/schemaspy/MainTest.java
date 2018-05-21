package org.schemaspy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.model.ConnectionFailure;
import org.schemaspy.model.Database;
import org.schemaspy.model.EmptySchemaException;
import org.schemaspy.testing.ExitCodeRule;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class MainTest {

    @Rule
    public ExitCodeRule exitCodeRule = new ExitCodeRule();

    private AnnotationConfigApplicationContext annotationConfigApplicationContext =
            new AnnotationConfigApplicationContext();

    private SchemaAnalyzer schemaAnalyzer = mock(SchemaAnalyzer.class);

    @Before
    public void setupContext() {
        annotationConfigApplicationContext.getDefaultListableBeanFactory().registerSingleton("schemaAnalyzer", schemaAnalyzer);
        annotationConfigApplicationContext.getDefaultListableBeanFactory().registerSingleton("loggingSystem", new LogbackLoggingSystem(ClassLoader.getSystemClassLoader()));
        annotationConfigApplicationContext.getDefaultListableBeanFactory().registerSingleton("applicationArguments", new DefaultApplicationArguments(new String [] {"-o", "target/mainTest", "-sso"}));
        annotationConfigApplicationContext.scan("org.schemaspy.cli");
        annotationConfigApplicationContext.register(SchemaSpyConfiguration.class);
        annotationConfigApplicationContext.refresh();
    }

    @Test
    public void ioExceptionExitCode_1() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new IOException("file permission error"));
        Main main = new Main();
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(main);
        ignoreSecurityException(()->main.run("-o", "target/mainTest", "-sso"));
        assertThat(exitCodeRule.getExitCode()).isEqualTo(1);
    }

    @Test
    public void emptySchemaExitCode_2() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new EmptySchemaException());
        Main main = new Main();
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(main);
        ignoreSecurityException(()->main.run("-o", "target/mainTest", "-sso"));
        assertThat(exitCodeRule.getExitCode()).isEqualTo(2);
    }

    @Test
    public void connectionFailureExitCode_3() throws IOException, SQLException {
        when(schemaAnalyzer.analyze(any(Config.class))).thenThrow(new ConnectionFailure("failed to connect"));
        Main main = new Main();
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(main);
        ignoreSecurityException(()->main.run("-o", "target/mainTest", "-sso"));
        assertThat(exitCodeRule.getExitCode()).isEqualTo(3);
    }

    @Test
    public void returnsNoneNullExitCode_0() throws IOException, SQLException {
        Database database = mock(Database.class);
        when(schemaAnalyzer.analyze(any(Config.class))).thenReturn(database);
        Main main = new Main();
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(main);
        ignoreSecurityException(()->main.run("-o", "target/mainTest", "-sso"));
        assertThat(exitCodeRule.getExitCode()).isEqualTo(0);
    }

    private void ignoreSecurityException(Runnable runnable) {
        try {
            runnable.run();
        } catch (SecurityException ignore) {}
    }

}
