/*
 * Copyright (C) 2023 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
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
