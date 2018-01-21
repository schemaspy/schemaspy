package org.schemaspy.model;

import org.junit.Test;
import org.testcontainers.shaded.io.netty.util.internal.StringUtil;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class InvalidConfigurationExceptionTest {

    @Test
    public void shouldSetParamName() {
        try {
            throw new InvalidConfigurationException("Unable to resolve databaseType: ").setParamName("-t");
        } catch (InvalidConfigurationException exception) {
            assertThat(exception.getParamName()).isEqualTo("-t");
        }
    }


    @Test(expected = InvalidConfigurationException.class)
    public void shouldThrowInvalidConfigurationExceptionWithMessage() {
        SimpleClass simpleClass = new SimpleClass();
        simpleClass.getParam();
    }

    @Test(expected = InvalidConfigurationException.class)
    public void shouldThrowInvalidConfigurationException() {
        invokeGetParam();
    }

    public void invokeGetParam() {
        try {
            SimpleClass simpleClass = new SimpleClass();
            simpleClass.getInstance().setParam(StringUtil.EMPTY_STRING);
        } catch (IllegalArgumentException exc) {
            throw new InvalidConfigurationException(exc);
        } catch (Exception exc) {
            if (exc.getCause() instanceof InvalidConfigurationException)
                throw (InvalidConfigurationException) exc.getCause();
            throw new InvalidConfigurationException(exc.getCause());
        }
    }

    class SimpleClass {
        private SimpleClass instance;
        private String param;

        public void createInstance() {
            instance = new SimpleClass();
        }

        public SimpleClass getInstance() throws InvalidConfigurationException  {
            return this.instance;
        }

        public String getParam() throws InvalidConfigurationException {
            try {
                return this.instance.param;
            } catch (Exception exc) {
                throw new InvalidConfigurationException("Failed to initialize instance of SimpleClass: ", exc);
            }
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
}