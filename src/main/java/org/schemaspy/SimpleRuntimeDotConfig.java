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

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.DotConfig;
import org.schemaspy.output.dot.RuntimeDotConfig;
import org.schemaspy.output.dot.schemaspy.FontConfig;
import org.schemaspy.output.dot.schemaspy.StyleSheet;

import java.util.Collection;

public class SimpleRuntimeDotConfig implements RuntimeDotConfig {

    private final FontConfig fontConfig;
    private final DotConfig dotConfig;
    private final boolean relativeLinks;
    private final boolean multiSchema;
    private final StyleSheet styleSheet;

    public SimpleRuntimeDotConfig(FontConfig fontConfig, DotConfig dotConfig, boolean relativeLinks, boolean multiSchema) {
        this.fontConfig = fontConfig;
        this.dotConfig = dotConfig;
        this.relativeLinks = relativeLinks;
        this.multiSchema = multiSchema;
        this.styleSheet = new StyleSheet(dotConfig.getTemplateDirectory(), dotConfig.getCss()).load();
    }

    @Override
    public boolean isRankDirBugEnabled() {
        return dotConfig.isRankDirBugEnabled();
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
        return dotConfig.isNumRowsEnabled();
    }

    @Override
    public boolean isOneOfMultipleSchemas() {
        return multiSchema;
    }

    @Override
    public StyleSheet styleSheet() {
        return styleSheet;
    }
    @Override
    public boolean showDetails(Collection<Table> table) {
        return table.size() <= dotConfig.getMaxDetailedTables();
    }
}
