package org.schemaspy.testing.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.OutputStreamAppender;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class LogbackExtension implements BeforeEachCallback, AfterEachCallback {

    private Logger logger;
    private Level preLevel;
    private boolean preAdditive;
    private final OutputStreamAppender outputStreamAppender = new OutputStreamAppender();
    private final PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final List<Matcher<? super String>> matchers = new ArrayList<>();

    public void expect(Matcher<? super String> matcher) {
        matchers.add(matcher);
    }
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        matchers.clear();
        findAnnotation(context.getElement(), Logback.class).ifPresent(logback -> {
            logger = getLogger(logback);
            preLevel = logger.getLevel();
            preAdditive = logger.isAdditive();
            logger.setLevel(Level.toLevel(logback.level(), preLevel));
            patternLayoutEncoder.setPattern(logback.pattern());
            patternLayoutEncoder.setContext(logger.getLoggerContext());
            outputStreamAppender.setEncoder(patternLayoutEncoder);
            outputStreamAppender.setOutputStream(byteArrayOutputStream);
            patternLayoutEncoder.start();
            outputStreamAppender.start();
            logger.addAppender(outputStreamAppender);
            logger.setAdditive(false);
        });
    }

    private Logger getLogger(Logback logback) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (!"Void".equals(logback.value().getSimpleName())) {
            return context.getLogger(logback.value());
        }
        if (!logback.logger().isBlank()) {
            return context.getLogger(logback.logger());
        }
        throw new ExtensionConfigurationException("Logback annotation requires either value or logger");
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        findAnnotation(context.getElement(), Logback.class).ifPresent(logback -> {
            outputStreamAppender.stop();
            patternLayoutEncoder.stop();
            logger.detachAppender(outputStreamAppender);
            logger.setAdditive(preAdditive);
            logger.setLevel(preLevel);
            if (!matchers.isEmpty()) {
                MatcherAssert.assertThat(byteArrayOutputStream.toString(), allOf(matchers));
            }
            byteArrayOutputStream.reset();
        });
    }

}
