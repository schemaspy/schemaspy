/*
 * Copyright (C) 2020 Nils Petzaell
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
package org.schemaspy.testing;

import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

public class IgnoreNonPrintedInCData implements DifferenceEvaluator {
    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) return outcome;
        if (comparison.getType() != ComparisonType.TEXT_VALUE) return outcome;
        if (comparison.getTestDetails().getTarget().getNodeType() == 4 && comparison.getControlDetails().getTarget().getNodeType() == 4) {
            String test = comparison.getTestDetails().getTarget().getTextContent().replaceAll("(\\s)*","");
            String control = comparison.getControlDetails().getTarget().getTextContent().replaceAll("(\\s)*","");
            if (test.equalsIgnoreCase(control)) {
                return ComparisonResult.EQUAL;
            }
        }
        return outcome;
    }
}
