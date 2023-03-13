package org.schemaspy.input.dbms;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import org.schemaspy.cli.NoRowsConfigCli;

import static org.assertj.core.api.Assertions.assertThat;

class DbmsConfigCliTest {

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
    void isEvaluateAllEnabled() {
        assertThat(
            parse("-all")
                .isEvaluateAllEnabled()
        )
            .isTrue();
    }

    @Test
    void isEvaluateAllEnabledDefault() {
        assertThat(
            parse()
                .isEvaluateAllEnabled()
        )
            .isFalse();
    }

    private DbmsConfig parse(String...args) {
        NoRowsConfigCli noRowsConfigCli = new NoRowsConfigCli();
        DbmsConfigCli dbmsConfigCli = new DbmsConfigCli(noRowsConfigCli);
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(dbmsConfigCli);
        jCommander.addObject(noRowsConfigCli);
        jCommander.parse(args);
        return dbmsConfigCli;
    }
}