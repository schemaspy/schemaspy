package org.schemaspy.util.markup;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.schemaspy.model.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MarkupProcessorTest {

    private static Stream<MarkupProcessor> provideMarkupProcessors() {
        return Stream.of(new Markdown(), new Asciidoc());
    }

    @ParameterizedTest
    @MethodSource("provideMarkupProcessors")
    public void replaceLinksWithExistingTablesAndDefaultRootPathTest(MarkupProcessor markupProcessor) {
        String sourceMarkdown = "See [table1] or [table2.column]";
        String expected = "<p>See <a href=\"./tables/table1.html\">table1</a>" +
                " or <a href=\"./tables/table2.html#column\">table2.column</a></p>";

        Collection<Table> tables = new ArrayList<>();
        Table table1 = mock(Table.class);
        given(table1.isLogical()).willReturn(false);
        given(table1.getName()).willReturn("table1");
        tables.add(table1);

        Table table2 = mock(Table.class);
        given(table2.isLogical()).willReturn(false);
        given(table2.getName()).willReturn("table2");
        tables.add(table2);

        markupProcessor.registryPage(tables);

        String actual = markupProcessor.toHtml(sourceMarkdown, "");

        assertThat(actual).contains(expected);
    }

    @ParameterizedTest
    @MethodSource("provideMarkupProcessors")
    public void replaceLinksWithExistingTablesAndOtherRootPathTest(MarkupProcessor markupProcessor) {
        String sourceMarkdown = "See [table1] or [table2.column]";
        String expected = "<p>See <a href=\"../root/tables/table1.html\">table1</a>" +
                " or <a href=\"../root/tables/table2.html#column\">table2.column</a></p>";

        Collection<Table> tables = new ArrayList<>();
        Table table1 = mock(Table.class);
        given(table1.isLogical()).willReturn(false);
        given(table1.getName()).willReturn("table1");
        tables.add(table1);

        Table table2 = mock(Table.class);
        given(table2.isLogical()).willReturn(false);
        given(table2.getName()).willReturn("table2");
        tables.add(table2);

        markupProcessor.registryPage(tables);

        String actual = markupProcessor.toHtml(sourceMarkdown, "../root");

        assertThat(actual).contains(expected);
    }

    @ParameterizedTest
    @MethodSource("provideMarkupProcessors")
    public void dontReplaceLinksWhenTableDoesntExists(MarkupProcessor markupProcessor) {
        String sourceMarkdown = "See [table1.column]";
        String expected = "<p>See [table1.column]</p>";

        String actual = markupProcessor.toHtml(sourceMarkdown, "");

        assertThat(actual).contains(expected);
    }
}