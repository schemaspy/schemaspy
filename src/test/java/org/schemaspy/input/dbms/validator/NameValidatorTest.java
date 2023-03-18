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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schemaspy.validator.NameValidator;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
class NameValidatorTest {

    private NameValidator nameValidator;

    @BeforeEach
    public void setup() {
        nameValidator = new NameValidator(
            "table",
            Pattern.compile(".*table.*"),
            Pattern.compile("excl.+"),
            new String[]{"TABLE"}
        );
    }

    @Test
    void valid() {
        assertThat(nameValidator.isValid("tablename", "table")).isTrue();
    }

    @Test
    void doesntMatchInclusion() {
        assertThat(nameValidator.isValid("doesntContainWord", "table")).isFalse();
    }

    @Test
    void excluded() {
        assertThat(nameValidator.isValid("exclude_table", "table")).isFalse();
    }

    @Test
    void typeDoesntMatch() {
        assertThat(nameValidator.isValid("tablename", "view")).isFalse();
    }

    @Test
    void defaultExcludesDollarSign() {
        assertThat(
            new NameValidator(
                "table",
                Pattern.compile(".*"),
                Pattern.compile(".*\\$.*"),
                new String[]{"TABLE"}
            )
                .isValid("abc$123", "TABLE")
        )
            .isFalse();
    }

    @Test
    void overrideDefaultIncludesDollarSign() {
        assertThat(
            new NameValidator(
                "table",
                Pattern.compile(".*"),
                Pattern.compile(""),
                new String[]{"TABLE"}
            )
                .isValid("abc$123", "TABLE")
        )
            .isTrue();
    }
}