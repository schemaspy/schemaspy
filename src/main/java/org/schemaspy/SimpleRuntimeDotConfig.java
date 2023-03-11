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
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.FontConfig;

public class SimpleRuntimeDotConfig implements RuntimeDotConfig {

    private final FontConfig fontConfig;
    private final boolean rankDirBugEnabled;
    private final boolean relativeLinks;
    private final boolean numRowsEnabled;
    private final boolean multiSchema;

    public SimpleRuntimeDotConfig(FontConfig fontConfig, DotConfig dotConfig, boolean relativeLinks, boolean multiSchema) {
        this.fontConfig = fontConfig;
        this.rankDirBugEnabled = dotConfig.isRankDirBugEnabled();
        this.relativeLinks = relativeLinks;
        this.numRowsEnabled = dotConfig.isNumRowsEnabled();
        this.multiSchema = multiSchema;
    }

    public SimpleRuntimeDotConfig(FontConfig fontConfig, boolean rankDirBugEnabled, boolean relativeLinks, boolean numRowsEnabled, boolean multiSchema) {
        this.fontConfig = fontConfig;
        this.rankDirBugEnabled = rankDirBugEnabled;
        this.relativeLinks = relativeLinks;
        this.numRowsEnabled = numRowsEnabled;
        this.multiSchema = multiSchema;
    }

    @Override
    public boolean isRankDirBugEnabled() {
        return rankDirBugEnabled;
    }

    @Override
    public String getFont() {
        return fontConfig.name();
    }

    @Override
    public int getFontSize() {
        return fontConfig.size();
    }

    public int getTextWidth(String text) {
        return fontConfig.widthOfText(text);
    }

    @Override
    public boolean useRelativeLinks() {
        return relativeLinks;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return numRowsEnabled;
    }

    @Override
    public boolean isOneOfMultipleSchemas() {
        return multiSchema;
    }
}
