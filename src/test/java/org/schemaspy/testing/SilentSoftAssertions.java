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
package org.schemaspy.testing;

import org.assertj.core.api.SoftAssertionError;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.extractProperty;

public class SilentSoftAssertions extends SoftAssertions {

    @Override
    public void assertAll() {
        List<Throwable> errors = errorsCollected();
        if (!errors.isEmpty()) {
            List<String> errorMessages = extractProperty("message", String.class)
                    .from(errors)
                    .stream()
                    .map(s -> s.split(System.lineSeparator(),2)[0])
                    .collect(Collectors.toList());
            throw new SoftAssertionError(errorMessages);
        }
    }
}