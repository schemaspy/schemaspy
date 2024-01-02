package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsciidocTest {

    @Test
    void toHtmlTest() {
        String sourceAsciiDoc = ":seq1: {counter:seq1}\n" +
                "== Section {seq1}\n" +
                "\n" +
                "The sequence in this section is {seq1}.\n" +
                "\n" +
                ":seq1: {counter:seq1}\n" +
                "== Section {seq1}\n" +
                "\n" +
                "The sequence in this section is {seq1}.";
        String expectedHtml = "<div class=\"sect1\">\n" +
                "<h2 id=\"_section_1\">Section 1</h2>\n" +
                "<div class=\"sectionbody\">\n" +
                "<div class=\"paragraph\">\n" +
                "<p>The sequence in this section is 1.</p>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<div class=\"sect1\">\n" +
                "<h2 id=\"_section_2\">Section 2</h2>\n" +
                "<div class=\"sectionbody\">\n" +
                "<div class=\"paragraph\">\n" +
                "<p>The sequence in this section is 2.</p>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>";

        String actualHtml = new Asciidoc(new PageRegistry(), sourceAsciiDoc, ".").value();
        assertThat(actualHtml).isEqualTo(expectedHtml);
    }

    @Test
    void formatsLinksCorrectly() {
        assertThat(
            new Asciidoc(new PageRegistry(), "", "").getLinkFormat().formatted("table1", "./tables/table1.html")
        ).isEqualTo("link:./tables/table1.html[table1]");
    }

    @Test
    void renderLinks() {
        assertThat(
            new Asciidoc(new PageRegistry(), "link:./tables/table1.html[table1]", "").value()
        ).contains("<a href=\"./tables/table1.html\">table1</a>");
    }
}