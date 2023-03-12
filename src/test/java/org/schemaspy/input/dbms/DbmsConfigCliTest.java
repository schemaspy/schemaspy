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