package org.schemaspy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RevisionTest {

	@Test
	public void revision() {
		Revision revision = new Revision();
		assertThat(revision.toString()).isEqualTo("Unknown");
	}

}