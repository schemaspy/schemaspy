package org.schemaspy.integrationtesting;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.Main;
import org.schemaspy.logging.LogLevelConditionalThrowableProxyConverter;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Main.class, StackTraceLoggingIT.StackTraceLoggingConfiguration.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,DirtiesContextBeforeModesTestExecutionListener.class,DirtiesContextTestExecutionListener.class})
public class StackTraceLoggingIT {

    static class StackTraceLoggingConfiguration {

        @Bean
        public ApplicationArguments applicationArguments(){
            return new DefaultApplicationArguments(new String[] {"-sso","-o","somefolder", "-t", "doesnt-exist"});
        }

        @Bean
        public LoggingSystem loggingSystem() {
            return new LogbackLoggingSystem(ClassLoader.getSystemClassLoader());
        }
    }

    @BeforeClass
    public static void fakeListener() {
        LogLevelConditionalThrowableProxyConverter logLevelConditionalThrowableProxyConverter = new LogLevelConditionalThrowableProxyConverter();
        logLevelConditionalThrowableProxyConverter.onApplicationEvent(null);
    }

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Autowired
    private Main main;

    @Autowired
    private ApplicationArguments applicationArguments;

    @Autowired
    private LoggingSystem loggingSystem;

    @Test
    @DirtiesContext
    @Logger(value = Main.class, pattern = "%clr(%-5level) - %msg%n%debugEx")
    public void noStacktraceWhenLoggingIsOf() {
        loggingSystem.setLogLevel("org.schemaspy", LogLevel.INFO);
        main.run(applicationArguments.getSourceArgs());
        String log = loggingRule.getLog();
        assertThat(log).isNotEmpty();
        assertThat(log).doesNotContain("Caused by: org.schemaspy.db.config.ResourceNotFoundException");
    }

    @Test
    @DirtiesContext
    @Logger(value = Main.class, pattern = "%clr(%-5level) - %msg%n%debugEx")
    public void stacktraceWhenLoggingIsOn() {
        loggingSystem.setLogLevel("org.schemaspy", LogLevel.DEBUG);
        try {
            main.run(applicationArguments.getSourceArgs());
            String log = loggingRule.getLog();
            assertThat(log).isNotEmpty();
            assertThat(log).contains("Caused by: org.schemaspy.db.config.ResourceNotFoundException");
        } finally {
            loggingSystem.setLogLevel("org.schemaspy", LogLevel.INFO);
        }
    }
}
