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
package org.schemaspy.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Watt
 */
class TableIndexTest {

	TableIndex tableIndex1;
	TableIndex tableIndex2;

	@BeforeEach
	void setup() {
		tableIndex1 = new TableIndex("index1", false);
		tableIndex2 = new TableIndex("index2", false);
	}

	@Test
	void primaryKeysSortBeforeNonPrimaryKeys() {
		tableIndex1.setIsPrimaryKey(true);
		tableIndex2.setIsPrimaryKey(false);

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	void stringIdComparison() {
		tableIndex1.setId("a");
		tableIndex2.setId("b");

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	void numericIdComparison() {
		tableIndex1.setId(1);
		tableIndex2.setId(3);

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-2);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(2);
	}

	@Test
	void nameComparison() {
		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	void stringIdEquality() {
		tableIndex1.setId("a");
		tableIndex2.setId("a");

		assertThat(tableIndex1).isEqualTo(tableIndex2);

		tableIndex2.setId("b");
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
	}

	@Test
	void numericIdEquality() {
		tableIndex1.setId(1);
		tableIndex2.setId(1);

		assertThat(tableIndex1).isEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isEqualTo(tableIndex2.hashCode());

		tableIndex2.setId(2);
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isNotEqualTo(tableIndex2.hashCode());
	}

	@Test
	void nameEquality() {
		tableIndex1 = new TableIndex("name", false);
		tableIndex2 = new TableIndex("name", false);
		assertThat(tableIndex1).isEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isEqualTo(tableIndex2.hashCode());

		tableIndex2 = new TableIndex("name2", false);
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isNotEqualTo(tableIndex2.hashCode());
	}

}