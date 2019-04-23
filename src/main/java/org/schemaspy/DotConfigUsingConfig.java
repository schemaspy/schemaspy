/*
 * Copyright (C) 2019 Nils Petzaell
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

import org.schemaspy.output.dot.DotConfig;

public class DotConfigUsingConfig implements DotConfig {

    private final Config config;
    private final boolean relativeLinks;

    public DotConfigUsingConfig(Config config, boolean relativeLinks) {
        this.config = config;
        this.relativeLinks = relativeLinks;
    }

    @Override
    public boolean isRankDirBugEnabled() {
        return config.isRankDirBugEnabled();
    }

    @Override
    public String getFont() {
        return config.getFont();
    }

    @Override
    public int getFontSize() {
        return config.getFontSize();
    }

    @Override
    public boolean useRelativeLinks() {
        return relativeLinks;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return config.isNumRowsEnabled();
    }

    @Override
    public boolean isOneOfMultipleSchemas() {
        return config.isOneOfMultipleSchemas();
    }
}
