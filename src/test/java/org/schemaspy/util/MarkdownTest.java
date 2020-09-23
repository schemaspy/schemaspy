package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownTest {

    @Test
    public void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = Markdown.toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    public void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = Markdown.toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).contains("<br />");
    }

}