package org.schemaspy.model;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TableIndexTest {

	TableIndex tableIndex1;
	TableIndex tableIndex2;

	@Before
	public void setup() {
		tableIndex1 = new TableIndex("index1", false);
		tableIndex2 = new TableIndex("index2", false);
	}

	@Test
	public void primaryKeysSortBeforeNonPrimaryKeys() {
		tableIndex1.setIsPrimaryKey(true);
		tableIndex2.setIsPrimaryKey(false);

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	public void stringIdComparison() {
		tableIndex1.setId("a");
		tableIndex2.setId("b");

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	public void numericIdComparison() {
		tableIndex1.setId(1);
		tableIndex2.setId(3);

		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-2);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(2);
	}

	@Test
	public void nameComparison() {
		assertThat(tableIndex1.compareTo(tableIndex2)).isEqualTo(-1);
		assertThat(tableIndex2.compareTo(tableIndex1)).isEqualTo(1);
	}

	@Test
	public void stringIdEquality() {
		tableIndex1.setId("a");
		tableIndex2.setId("a");

		assertThat(tableIndex1).isEqualTo(tableIndex2);

		tableIndex2.setId("b");
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
	}

	@Test
	public void numericIdEquality() {
		tableIndex1.setId(1);
		tableIndex2.setId(1);

		assertThat(tableIndex1).isEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isEqualTo(tableIndex2.hashCode());

		tableIndex2.setId(2);
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isNotEqualTo(tableIndex2.hashCode());
	}

	@Test
	public void nameEquality() {
		tableIndex1 = new TableIndex("name", false);
		tableIndex2 = new TableIndex("name", false);
		assertThat(tableIndex1).isEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isEqualTo(tableIndex2.hashCode());

		tableIndex2 = new TableIndex("name2", false);
		assertThat(tableIndex1).isNotEqualTo(tableIndex2);
		assertThat(tableIndex1.hashCode()).isNotEqualTo(tableIndex2.hashCode());
	}

}