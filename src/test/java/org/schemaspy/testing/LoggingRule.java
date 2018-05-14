/*
 * Copyright (C) 2017 Nils Petzaell
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

/**
 * @author Nils Petzaell
 */
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
        logger.setAdditive(false);
    }

    private void after() {
        outputStreamAppender.stop();
        patternLayoutEncoder.stop();
        logger.detachAppender(outputStreamAppender);
        logger.setAdditive(true);
    }
}