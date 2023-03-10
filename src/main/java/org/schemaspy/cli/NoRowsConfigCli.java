package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(resourceBundle = "norowsconfigcli")
public class NoRowsConfigCli {

    @Parameter(
        names = {
            "-norows", "--no-rows",
            "schemaspy.norows", "schemaspy.no-rows"
        },
        descriptionKey = "norows"
    )
    private boolean noRows = false;

    public boolean isNumRowsEnabled() {
        return !noRows;
    }
}
