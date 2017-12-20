package org.schemaspy.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogTest {

	@Test
	public void compareEqualsHash() {
		Catalog cat1 = new Catalog("123");
		Catalog cat2 = new Catalog("456");

		assertThat(cat1).isNotEqualTo(cat2);
		assertThat(cat1.hashCode()).isNotEqualTo(cat2.hashCode());
		assertThat(cat1.compareTo(cat2)).isLessThan(0);
	}

	@Test
	public void equal() {
		Catalog cat1 = new Catalog("123");
		Catalog cat2 = new Catalog("123");

		assertThat(cat1).isEqualTo(cat2);
		assertThat(cat1.hashCode()).isEqualTo(cat2.hashCode());
		assertThat(cat1.compareTo(cat2)).isEqualTo(0);
	}

}