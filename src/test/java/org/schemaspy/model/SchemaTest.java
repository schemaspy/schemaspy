package org.schemaspy.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class SchemaTest {
	@Test
	public void compareEqualsHash() {
		EqualsVerifier.forClass(Schema.class).withNonnullFields("name").withIgnoredFields("comment").verify();
	}
}