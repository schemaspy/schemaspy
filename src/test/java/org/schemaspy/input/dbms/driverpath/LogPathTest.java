package org.schemaspy.input.dbms.driverpath;

import java.nio.file.Paths;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.schemaspy.testing.RecordingLogger;
import org.schemaspy.testing.SimpleLogEvent;
import org.slf4j.event.Level;

import static org.assertj.core.api.Assertions.assertThat;

class LogPathTest {

  @ParameterizedTest
  @EnumSource(Level.class)
  void testLevelsAllWillLog(Level level) {
    RecordingLogger recordingLogger = new RecordingLogger();
    new LogPath("path: {}", recordingLogger, level).accept(Paths.get("src"));
    assertThat(
        recordingLogger.events()
    )
        .hasSize(1)
        .containsExactly(new SimpleLogEvent(level, "path: src"));
  }

  @ParameterizedTest
  @EnumSource(Level.class)
  void testLevelNoneLogs(Level level) {
    RecordingLogger recordingLogger = new RecordingLogger(false);
    new LogPath("path: {}", recordingLogger, level).accept(Paths.get("src"));
    assertThat(
        recordingLogger.events()
    )
        .hasSize(0);
  }

}