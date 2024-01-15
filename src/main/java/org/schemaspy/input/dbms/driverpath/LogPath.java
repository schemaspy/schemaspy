package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Path;
import java.util.function.Consumer;

import org.schemaspy.logging.Sanitize;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class LogPath implements Consumer<Path> {

  private final String format;
  private final Logger logger;
  private final Level level;

  public LogPath(
      final String format,
      final Logger logger,
      final Level level
  ) {
    this.format = format;
    this.logger = logger;
    this.level = level;
  }

  @Override
  public void accept(final Path path) {
    if (logger.isEnabledForLevel(level)) {
      Sanitize sanitizePath = new Sanitize(path.toString());
      switch (level) {
        case ERROR -> logger.error(format, sanitizePath);
        case WARN -> logger.warn(format, sanitizePath);
        case INFO -> logger.info(format, sanitizePath);
        case DEBUG -> logger.debug(format, sanitizePath);
        case TRACE -> logger.trace(format, sanitizePath);
      }
    }
  }
}
