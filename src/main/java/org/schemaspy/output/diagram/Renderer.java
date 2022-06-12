
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

import java.io.File;

/**
 * Abstraction for rendering graphics from dot-language
 * @author Nils Petzaell
 */
public interface Renderer {

    /**
     * Textual identifier of implementation
     * @return String
     */
    String identifier();

    /**
     * Render file written in dot language to graphics
     * @param dotFile file written in dot language
     * @param diagramFile destination for graphics
     * @return html map for use with image
     */
    String render(File dotFile, File diagramFile);

    /**
     * Graphics format of output
     * @return
     */
    String format();
}