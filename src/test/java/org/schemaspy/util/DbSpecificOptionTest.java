package org.schemaspy.util;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.junit.Assert.assertThat;


public class DbSpecificOptionTest {

    @Test
    public void valueOfOptionCanBeNull() {
        DbSpecificOption dbSpecificOption = new DbSpecificOption("MyOption", "MyDescription");
        assertThat(dbSpecificOption.getValue(), IsNull.nullValue());
    }
}