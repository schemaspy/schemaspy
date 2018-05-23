/*
 * Copyright (C) 2017 Rafal Kasa
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
package org.schemaspy.model;

import org.junit.Test;
import org.testcontainers.shaded.io.netty.util.internal.StringUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rafal Kasa
 */
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