package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTest {

    @Test
    void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = new Markdown(new PageRegistry()).toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = new Markdown(new PageRegistry()).toHtml(sourceMarkdown, ".");
        assertThat(renderedMarkdown).contains("<br />");
    }

    @Test
    void formatsLinksCorrectly() {
        assertThat(
            new Markdown(new PageRegistry()).getLinkFormat().formatted("table1", "./tables/table1.html")
        ).isEqualTo("[table1](./tables/table1.html)");
    }

    @Test
    void renderLinks() {
        assertThat(
            new Markdown(new PageRegistry()).toHtml("[table1](./tables/table1.html)", "")
        ).contains("<a href=\"./tables/table1.html\">table1</a>");
    }
}