package org.schemaspy.view;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlFormatterTest {

    @Test
    public void escapeHtml() {
        assertThat(HtmlFormatter.escapeHtml("string")).isEqualTo("string");
        assertThat(HtmlFormatter.escapeHtml("string with spaces")).isEqualTo("string with spaces");
    }

    @Test
    public void urlEncodeLink() {
        assertThat(HtmlFormatter.urlEncodeLink("string")).isEqualTo("string");
        assertThat(HtmlFormatter.urlEncodeLink("string with spaces")).isEqualTo("string%20with%20spaces");
    }
}