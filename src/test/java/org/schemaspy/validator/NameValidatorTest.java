package org.schemaspy.validator;

import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void excludeTablesWithDollarSigns() {
        assertThat(nameValidator.isValid("table$name","table")).isFalse();
    }
}