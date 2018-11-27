package org.schemaspy.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CombinedDefaultProviderTest {

    @Test
    public void returnsNullIfNullIsEverything() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> null);
        String value = combinedDefaultProvider.getDefaultValueFor("someString");
        assertThat(value).isNull();
    }

    @Test
    public void getSSOWithOutValueShouldBeTrue() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> "");
        assertThat(combinedDefaultProvider.getDefaultValueFor("schemaspy.sso")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    public void getDebugWithValueFalseShouldBeFalse() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> "false");
        assertThat(combinedDefaultProvider.getDefaultValueFor("schemaspy.debug")).isEqualTo(Boolean.FALSE.toString());
    }

    @Test
    public void getNoHTMLWithoutValueIsTrue() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> "");
        assertThat(combinedDefaultProvider.getDefaultValueFor("schemaspy.nohtml")).isEqualTo(Boolean.TRUE.toString());
    }

    @Test
    public void getNoHTMLNotDefinedIsFalse() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> null);
        assertThat(combinedDefaultProvider.getDefaultValueFor("schemaspy.nohtml")).isEqualTo(Boolean.FALSE.toString());
    }

    @Test
    public void definedOrderIsTheOrder() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> "first",(optionName)-> "second" );
        assertThat(combinedDefaultProvider.getDefaultValueFor("someString")).isEqualTo("first");
    }

    @Test
    public void willFallbackToSecondIfFirstIsNull() {
        CombinedDefaultProvider combinedDefaultProvider = new CombinedDefaultProvider((optionName)-> null,(optionName)-> "second" );
        assertThat(combinedDefaultProvider.getDefaultValueFor("someString")).isEqualTo("second");
    }

}