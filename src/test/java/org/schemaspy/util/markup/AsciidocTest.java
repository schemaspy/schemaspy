package org.schemaspy.util.markup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

        String actualHtml = new Asciidoc().toHtml(sourceAsciiDoc, ".");
        assertThat(actualHtml).isEqualTo(expectedHtml);
    }

    @Test
    void referenceLinksAreHandledProperly() {
        String sourceAsciiDoc = "Refer to xref:document-b.adoc#section-b[Section B] for more information.";
        String expectedHtml = "<div class=\"paragraph\">\n" +
                "<p>Refer to <a href=\"./schema2/document-b.html#section-b\">Section B</a> for more information.</p>\n" +
                "</div>";

        String actualHtml = new Asciidoc().toHtml(sourceAsciiDoc, "./schema2");
        assertThat(actualHtml).isEqualTo(expectedHtml);
    }
}