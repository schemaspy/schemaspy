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

    private DbmsConfig parse(String...args) {
        DbmsConfigCli dbmsConfigCli = new DbmsConfigCli(new NoRowsConfigCli());
        new JCommander(dbmsConfigCli).parse(args);
        return dbmsConfigCli;
    }
}