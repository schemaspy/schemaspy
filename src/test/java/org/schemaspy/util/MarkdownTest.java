package org.schemaspy.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTest {

    @Test
    void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = new Markdown(sourceMarkdown, ".").toHtml();
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = new Markdown(sourceMarkdown, ".").toHtml();
        assertThat(renderedMarkdown).contains("<br />");
    }
}