package org.schemaspy.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assume.assumeThat;

public class EnvDefaultProviderTest {

    private EnvDefaultProvider envDefaultProvider = new EnvDefaultProvider();

    @Test
    public void returnsNullWhenNothingExists() {
        String value = envDefaultProvider.getDefaultValueFor("this_should_not_exist");
        assertThat(value).isNull();
    }

    @Test
    public void willReturnPATH() {
        assumeThat(System.getProperty("os.name"), is("Linux"));
        String value = envDefaultProvider.getDefaultValueFor("PATH");
        assertThat(value).isNotNull();
    }

    @Test
    public void willReturnPath() {
        assumeThat(System.getProperty("os.name"), startsWith("Windows"));
        String value = envDefaultProvider.getDefaultValueFor("Path");
        assertThat(value).isNotNull();
    }

}