package org.schemaspy.input.dbms;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.cli.NoRowsConfigCli;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingConfigCliTest {

    @Test
    void databaseType() {
        assertThat(
            parse("-t", "myType")
                .getDatabaseType()
        )
            .isEqualTo("myType");
    }

    @Test
    void databaseTypeProperties() {
        Properties properties = new Properties();
        properties.put("correct", "true");
        assertThat(
            parse(properties, "-t", "myType")
                .getDatabaseTypeProperties()
        )
            .containsEntry("correct", "true");
    }

    @Test
    void isExportedKeysEnabled() {
        assertThat(
            parse("-noexportedkeys")
                .isExportedKeysEnabled()
        )
            .isFalse();
    }

    @Test
    void isExportedKeysEnabledDefault() {
        assertThat(
            parse()
                .isExportedKeysEnabled()
        )
            .isTrue();
    }

    @Test
    void isNumRowsEnabled() {
        assertThat(
            parse("-norows")
                .isNumRowsEnabled()
        )
            .isFalse();
    }

    @Test
    void isNumRowsEnabledDefault() {
        assertThat(
            parse()
                .isNumRowsEnabled()
        )
            .isTrue();
    }

    @Test
    void isViewsEnabled() {
        assertThat(
            parse("-noviews")
                .isViewsEnabled()
        )
            .isFalse();
    }

    @Test
    void isViewsEnabledDefault() {
        assertThat(
            parse()
                .isViewsEnabled()
        )
            .isTrue();
    }

    @Test
    void getColumnExclusions() {
        assertThat(
            parse("-X","^exclude.*")
                .getColumnExclusions()
                .pattern()
        )
            .isEqualTo("^exclude.*");
    }

    @Test
    void getColumnExclusionsDefault() {
        assertThat(
            parse()
                .getColumnExclusions()
                .pattern()
        )
            .isEqualTo("[^.]");
    }

    @Test
    void getIndirectColumnExclusions() {
        assertThat(
            parse("-x","^exclude.*")
                .getIndirectColumnExclusions()
                .pattern()
        )
            .isEqualTo("^exclude.*");
    }

    @Test
    void getIndirectColumnExclusionsDefault() {
        assertThat(
            parse()
                .getIndirectColumnExclusions()
                .pattern()
        )
            .isEqualTo("[^.]");
    }

    @Test
    void getTableInclusions() {
        assertThat(
            parse("-i", "abc.*")
                .getTableInclusions()
                .pattern()
        )
            .isEqualTo("abc.*");
    }

    @Test
    void getTableInclusionsDefault() {
        assertThat(
            parse()
                .getTableInclusions()
                .pattern()
        )
            .isEqualTo(".*");
    }

    @Test
    void getTableExclusion() {
        assertThat(
            parse("-I", "abc.*")
                .getTableExclusions()
                .pattern()
        )
            .isEqualTo("abc.*");
    }

    @Test
    void getTableExclusionDefault() {
        assertThat(
            parse()
                .getTableExclusions()
                .pattern()
        )
            .isEqualTo(".*\\$.*");
    }

    @Test
    void getMaxDbThreads() {
        assertThat(
            parse("-dbThreads", "5")
                .getMaxDbThreads()
        )
            .isEqualTo(5);
    }

    @Test
    void getMaxDbThreadsFromDatabaseType() {
        Properties properties = new Properties();
        properties.put("dbThreads", "12");
        assertThat(
            parse(properties, "-t", "myType")
                .getMaxDbThreads()
        )
            .isEqualTo(12);
    }

    @Test
    void getMaxDbThreadsDefault() {
        int expected = Math.min(Runtime.getRuntime().availableProcessors(), 15);
        assertThat(
            parse()
                .getMaxDbThreads()
        )
            .isEqualTo(expected);
    }

    private ProcessingConfig parse(String...args) {
        return parse(new Properties(), args);
    }

    private ProcessingConfig parse(Properties properties, String...args) {
        NoRowsConfigCli noRowsConfigCli = new NoRowsConfigCli();
        DatabaseTypeConfigCli databaseTypeConfigCli = new DatabaseTypeConfigCli(((databaseType) -> databaseType.equals("myType") ? properties : new Properties()));
        ProcessingConfigCli analysisConfigCli = new ProcessingConfigCli(noRowsConfigCli, databaseTypeConfigCli);
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(noRowsConfigCli);
        jCommander.addObject(databaseTypeConfigCli);
        jCommander.addObject(analysisConfigCli);
        jCommander.parse(args);
        return analysisConfigCli;
    }

}