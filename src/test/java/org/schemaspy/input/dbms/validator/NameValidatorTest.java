/*
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.input.dbms.validator;

import org.junit.Before;
import org.junit.Test;
import org.schemaspy.validator.NameValidator;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
public class NameValidatorTest {

    private static final Pattern includeWithTable = Pattern.compile(".*table.*");
    private static final Pattern exclude          = Pattern.compile("excl.+");

    NameValidator nameValidator;

    @Before
    public void setup() {
        nameValidator = new NameValidator("table", includeWithTable, exclude, new String[]{"TABLE"});
    }

    @Test
    public void valid() {
        assertThat(nameValidator.isValid("tablename","table")).isTrue();
    }

    @Test
    public void doesntMatchInclusion() {
        assertThat(nameValidator.isValid("doesntContainWord","table")).isFalse();
    }

    @Test
    public void excluded() {
        assertThat(nameValidator.isValid("exclude_table","table")).isFalse();
    }

    @Test
    public void typeDoesntMatch() {
        assertThat(nameValidator.isValid("tablename","view")).isFalse();
    }
}