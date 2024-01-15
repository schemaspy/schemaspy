package org.schemaspy.testing;

import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.TypedArgumentConverter;

public class YesNoConverter extends TypedArgumentConverter<String, Boolean> {
  protected YesNoConverter() {
    super(String.class, Boolean.class);
  }

  @Override
  protected Boolean convert(final String source) throws ArgumentConversionException {
    return "yes".equalsIgnoreCase(source);
  }
}
