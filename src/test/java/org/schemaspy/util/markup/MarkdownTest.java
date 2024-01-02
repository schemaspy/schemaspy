package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

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

    @Test
    void formatsLinksCorrectly() {
        assertThat(
            new Markdown().getLinkFormat().formatted("table1", "./tables/table1.html")
        ).isEqualTo("[table1](./tables/table1.html)");
    }
}