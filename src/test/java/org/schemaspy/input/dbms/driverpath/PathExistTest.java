package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Paths;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.schemaspy.testing.YesNoConverter;

import static org.assertj.core.api.Assertions.assertThat;

class PathExistTest {
  @ParameterizedTest
  @CsvSource(
      useHeadersInDisplayName = true,
      textBlock = """
      path,           should exist
      src,            yes
      doesNotExist,   no
      """
  )
  void testPathExistence(String strPath, @ConvertWith(YesNoConverter.class) boolean shouldExist) {
    assertThat(
        new PathExist().test(Paths.get(strPath))
    ).isEqualTo(shouldExist);
  }

}