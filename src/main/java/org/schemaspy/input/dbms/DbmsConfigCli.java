package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

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

    public boolean isExportedKeysEnabled() {
        return !noExportedKeys;
    }
}
