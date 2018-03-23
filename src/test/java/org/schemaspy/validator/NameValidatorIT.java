package org.schemaspy.validator;

import org.junit.Test;
import org.schemaspy.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class NameValidatorIT {

    @Test
    public void defaultExcludesDollarSign() {
        Config config = new Config();
        NameValidator nameValidator = new NameValidator("table", config.getTableInclusions(), config.getTableExclusions(), new String[]{"TABLE"});
        boolean valid = nameValidator.isValid("abc$123", "TABLE");
        assertThat(valid).isFalse();
    }

    @Test
    public void overrideDefaultIncludesDollarSign() {
        Config config = new Config("-I", "");
        NameValidator nameValidator = new NameValidator("table", config.getTableInclusions(), config.getTableExclusions(), new String[]{"TABLE"});
        boolean valid = nameValidator.isValid("abc$123", "TABLE");
        assertThat(valid).isTrue();
    }
}
