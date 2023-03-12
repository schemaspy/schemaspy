package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;

import java.util.regex.Pattern;

@Parameters(resourceBundle = "dbmsconfigcli")
public class DbmsConfigCli implements DbmsConfig {

    @Parameter(
        names = {
            "-noexportedkeys", "--no-exported-keys",
            "schemaspy.noexportedkeys", "schemaspy.no-exported-keys"
        },
        descriptionKey = "noexportedkeys"
    )
    private boolean noExportedKeys = false;

    @Parameter(
        names = {
            "-noviews", "--no-views",
            "schemaspy.noviews", "schemaspy.no-views"
        },
        descriptionKey = "noviews"
    )
    private boolean noViews = false;

    private NoRowsConfigCli noRowsConfigCli;

    public DbmsConfigCli(NoRowsConfigCli noRowsConfigCli) {
        this.noRowsConfigCli = noRowsConfigCli;
    }

    public boolean isExportedKeysEnabled() {
        return !noExportedKeys;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return noRowsConfigCli.isNumRowsEnabled();
    }

    @Override
    public boolean isViewsEnabled() {
        return !noViews;
    }
}
