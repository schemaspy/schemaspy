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
package org.schemaspy.output.html.mustache.diagrams;

import org.schemaspy.output.OutputException;
import org.schemaspy.view.MustacheTableDiagram;

import java.util.List;

/**
 * @author Nils Petzaell
 */
public class MustacheSummaryDiagramResults {

    private final List<MustacheTableDiagram> diagrams;
    private final boolean hasRealRelationships;
    private final List<OutputException> outputExceptions;

    public MustacheSummaryDiagramResults(List<MustacheTableDiagram> diagrams, boolean hasRealRelationships, List<OutputException> outputExceptions) {
        this.diagrams = diagrams;
        this.hasRealRelationships = hasRealRelationships;
        this.outputExceptions = outputExceptions;
    }

    public List<MustacheTableDiagram> getDiagrams() {
        return diagrams;
    }

    public boolean hasRealRelationships() {
        return hasRealRelationships;
    }

    public List<OutputException> getOutputExceptions() {
        return outputExceptions;
    }
}