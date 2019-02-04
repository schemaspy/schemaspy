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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.schemaspy.testing;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input.Builder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

public class XmlOutputDiff {

    private XmlOutputDiff() {};

    public static Diff diffXmlOutput(Builder actually, Builder expected) {
        return DiffBuilder.compare(expected)
                .normalizeWhitespace()
                .withTest(actually)
                .withDifferenceEvaluator(DifferenceEvaluators.chain(DifferenceEvaluators.Default, new IgnoreUsingXPath("/database[1]/@type")))
                .build();
    }
}
