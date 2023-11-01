package org.schemaspy.input.dbms.driverpath;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;

/**
 * Tests for {@link DpMissingPathChecked}.
 */
public class DpMissingPathCheckedTest {

    /**
     * Given a driverpath with missing paths,
     * When the object is asked for a driverpath,
     * Then it should log a warning.
     */
    @Test
    public void warn()  {
        final Logger logger = Mockito.mock(Logger.class);
        final String driverPath = Paths.get("src", "test", "resources", "driverFolder", "dummy.jar")
                .toString() + File.pathSeparator + "missing";
        new DpMissingPathChecked(logger, () -> driverPath).value();
        Mockito.verify(logger).warn(any());
    }
}
