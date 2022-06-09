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
package org.schemaspy.output.diagram;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nils Petzaell
 */
public class DiagramResult {

    private static final Pattern MAP_NAME_PATTERN = Pattern.compile("<map.*name=\"([\\w\\s]+).*");
    private final String fileName;
    private final String map;
    private final String imageFormat;

    public DiagramResult(String fileName, String map, String imageFormat) {
        this.fileName = fileName;
        this.map = map;
        this.imageFormat = imageFormat;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMapName() {
        if (Objects.nonNull(map)) {
            Matcher matcher = MAP_NAME_PATTERN.matcher(map);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    public String getMap() {
        return Objects.isNull(map) ? "" : map.trim();
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public boolean isEmbed() {
        return "svg".equalsIgnoreCase(imageFormat);
    }
}