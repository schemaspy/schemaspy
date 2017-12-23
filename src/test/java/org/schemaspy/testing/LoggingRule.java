package org.schemaspy.testing;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LoggingRule implements TestRule {

    private Logger config;

    private ch.qos.logback.classic.Logger logger;
    private OutputStreamAppender outputStreamAppender = new OutputStreamAppender();
    private PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public String getLog() {
        return getLog(StandardCharsets.UTF_8);
    }

    public String getLog(Charset charset) {
        return new String(byteArrayOutputStream.toByteArray(), charset);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        config = description.getAnnotation(Logger.class);
        if (Objects.isNull(config)) {
            return base;
        }
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

    private void before() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(config.value());
        patternLayoutEncoder.setPattern(config.pattern());
        patternLayoutEncoder.setContext(logger.getLoggerContext());
        outputStreamAppender.setEncoder(patternLayoutEncoder);
        outputStreamAppender.setOutputStream(byteArrayOutputStream);
        patternLayoutEncoder.start();
        outputStreamAppender.start();
        logger.addAppender(outputStreamAppender);
    }

    private void after() {
        outputStreamAppender.stop();
        patternLayoutEncoder.stop();
        logger.detachAppender(outputStreamAppender);
    }
}