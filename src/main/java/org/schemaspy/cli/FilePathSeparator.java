package org.schemaspy.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.converters.IParameterSplitter;

public class FilePathSeparator implements IParameterSplitter {
  @Override
  public List<String> split(final String value) {
    return Arrays.asList(value.split(File.pathSeparator));
  }
}
