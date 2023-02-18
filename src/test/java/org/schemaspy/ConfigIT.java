/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.system.OutputCaptureRule;

/**
 * @author Nils Petzaell
 */
public class ConfigIT {

    @Rule
    public OutputCaptureRule outputCapture = new OutputCaptureRule();

    @Test
    public void onlyOutputSelectedDatabaseTypeWhenDbSpecific() {
        outputCapture.expect(Matchers.containsString("MySQL"));
        outputCapture.expect(Matchers.not(Matchers.containsString("Microsoft SQL Server")));
        Config config = new Config("-t", "mysql");
        config.dumpUsage("Test", true);
    }

}
