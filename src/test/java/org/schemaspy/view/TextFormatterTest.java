package org.schemaspy.view;

import java.io.PrintWriter;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.schemaspy.model.Table;

/**
 * Tests for {@link TextFormatter}.
 */
class TextFormatterTest {

    /**
     * Given a table and an output stream,
     * When the object is asked to print,
     * Then it should delegate to the output stream.
     */
    @Test
    void printTable() {
        final Table table = mock(Table.class);
        final PrintWriter out = mock(PrintWriter.class);
        new TextFormatter(List.of(table), false, out).write();
        verify(out, times(1)).println(nullable(String.class));
    }

    /**
     * Given a view and instructions not to print views,
     * When the object is asked to print,
     * Then it should not delegate to the output stream.
     */
    @Test
    void ignoreView() {
        final Table table = mock(Table.class);
        when(table.isView()).thenReturn(true);
        final PrintWriter out = mock(PrintWriter.class);
        new TextFormatter(List.of(table), false, out).write();
        verify(out, times(0)).println(nullable(String.class));
    }
}
