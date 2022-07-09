package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkdownTest {

    @Test
    public void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = new Markdown(sourceMarkdown, ".").toHtml();
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    public void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = new Markdown(sourceMarkdown, ".").toHtml();
        assertThat(renderedMarkdown).contains("<br />");
    }

}