package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownTest {

    @Test
    void willNotReplaceDirectNewLineToBR() {
        String sourceMarkdown = "Line\n with\n no\n hardbreak\n";
        String renderedMarkdown = new Markdown(new MarkupFromString(sourceMarkdown)).value();
        assertThat(renderedMarkdown).doesNotContain("<br />");
    }

    @Test
    void willReplaceNewLineWhenPrecededByTwoSpacesAsBR() {
        String sourceMarkdown = "Line  \n with  \n no  \n hardbreak  \n";
        String renderedMarkdown = new Markdown(new MarkupFromString(sourceMarkdown)).value();
        assertThat(renderedMarkdown).contains("<br />");
    }

    @Test
    void formatsLinksCorrectly() {
        assertThat(
            Markdown.LINK_FORMAT.formatted("table1", "./tables/table1.html")
        ).isEqualTo("[table1](./tables/table1.html)");
    }

    @Test
    void renderLinks() {
        assertThat(
            new Markdown(new MarkupFromString("[table1](./tables/table1.html)")).value()
        ).contains("<a href=\"./tables/table1.html\">table1</a>");
    }
}