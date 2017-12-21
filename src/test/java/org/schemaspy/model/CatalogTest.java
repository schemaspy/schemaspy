package org.schemaspy.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogTest {

	@Test
	public void compareEqualsHash() {
		EqualsVerifier.forClass(Catalog.class).withNonnullFields("name").withIgnoredFields("comment").verify();
	}
}