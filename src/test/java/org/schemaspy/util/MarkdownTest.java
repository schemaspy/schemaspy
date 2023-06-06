package org.schemaspy.util;

import org.junit.jupiter.api.Test;
import org.schemaspy.util.markup.Markdown;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTest {

    @Test
    void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = new Markdown().toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = new Markdown().toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).contains("<br />");
    }
}