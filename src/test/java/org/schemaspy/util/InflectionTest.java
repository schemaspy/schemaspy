package org.schemaspy.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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