/*
 * Copyright (C) 2017 Daniel Watt
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
package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
public class InflectionTest {

	@Test
	public void uncountable() {
		assertThat(Inflection.isUncountable("equipment")).isTrue();
		assertThat(Inflection.isUncountable("test")).isFalse();
	}

	@Test
	public void pluralize() {
		assertThat(Inflection.pluralize("test")).isEqualTo("tests");
		assertThat(Inflection.pluralize("equipment")).isEqualTo("equipment");
	}

	@Test
	public void singularize() {
		assertThat(Inflection.singularize("tests")).isEqualTo("test");
		assertThat(Inflection.singularize("equipment")).isEqualTo("equipment");
	}



}