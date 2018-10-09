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

import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.WriteStats;

import java.util.List;

public class MustacheSummaryDiagramResults {

    private final List<MustacheTableDiagram> diagrams;
    private final WriteStats stats;
    private final boolean hasRealRelationships;
    private final List<ImpliedForeignKeyConstraint> impliedConstraints;

    public MustacheSummaryDiagramResults(List<MustacheTableDiagram> diagrams, WriteStats stats, boolean hasRealRelationships, List<ImpliedForeignKeyConstraint> impliedConstraints) {
        this.diagrams = diagrams;
        this.stats = stats;
        this.hasRealRelationships = hasRealRelationships;
        this.impliedConstraints = impliedConstraints;
    }

    public List<MustacheTableDiagram> getDiagrams() {
        return diagrams;
    }

    public WriteStats getStats() {
        return stats;
    }

    public boolean hasRealRelationships() {
        return hasRealRelationships;
    }

    public List<ImpliedForeignKeyConstraint> getImpliedConstraints() {
        return impliedConstraints;
    }
}