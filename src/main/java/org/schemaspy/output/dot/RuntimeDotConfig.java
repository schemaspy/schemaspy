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
package org.schemaspy.output.dot;

import org.schemaspy.model.Table;
import org.schemaspy.output.dot.schemaspy.StyleSheet;

import java.util.Collection;

public interface RuntimeDotConfig {
    boolean isRankDirBugEnabled();
    String getFont();
    int getFontSize();
    int getTextWidth(String text);
    boolean useRelativeLinks();
    boolean isNumRowsEnabled();
    boolean isOneOfMultipleSchemas();
    StyleSheet styleSheet();
    boolean showDetails(Collection<Table> table);
    boolean isHideLabelEnabled();
}
