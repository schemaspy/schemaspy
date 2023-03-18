package org.schemaspy.output.dot.schemaspy;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.SimpleRuntimeDotConfig;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.TemplateDirectoryConfigCli;
import org.schemaspy.model.Database;
import org.schemaspy.model.LogicalTable;
import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.DotConfigCli;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrphanGraphTest {

    @Test
    void orphanGraphContainsOnlyOrphan() {
        assertThat(
                new OrphanGraph(
                        new SimpleRuntimeDotConfig(
                                new TestFontConfig(),
                                parse("-rankdirbug"),
                                false,
                                false
                        ),
                        Arrays.asList(mockView(),realTable(), mockNotOrphan())
                ).dot()
        )
                .containsOnlyOnce("label=<");
    }


    private Table mockView() {
        Table table = mock(Table.class);
        when(table.isView()).thenReturn(true);
        return table;
    }

    private Table mockNotOrphan() {
        Table table = mock(Table.class);
        when(table.isOrphan(anyBoolean())).thenReturn(false);
        return table;
    }

    private Table realTable() {
        return new LogicalTable(mock(Database.class), "cat", "sch", "tab", "com");
    }

    private DotConfig parse(String... args) {
        NoRowsConfigCli noRowsConfigCli = new NoRowsConfigCli();
        TemplateDirectoryConfigCli templateDirectoryConfigCli = new TemplateDirectoryConfigCli();
        DotConfigCli dotConfigCli = new DotConfigCli(noRowsConfigCli, templateDirectoryConfigCli);
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(noRowsConfigCli);
        jCommander.addObject(templateDirectoryConfigCli);
        jCommander.addObject(dotConfigCli);
        jCommander.parse(args);
        return dotConfigCli;
    }

}