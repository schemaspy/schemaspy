package org.schemaspy.input.dbms.driverpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public final class DpMissingPathChecked implements Driverpath {

    private final Logger logger;
    private final Driverpath origin;

    public DpMissingPathChecked(final Driverpath origin) {
        this(LoggerFactory.getLogger(MethodHandles.lookup().lookupClass()), origin);
    }

    public DpMissingPathChecked(final Logger logger, final Driverpath origin) {
        this.logger = logger;
        this.origin = origin;
    }

    @Override
    public String value() {
        final String value = this.origin.value();
        missingPathsMessage(value);
        return value;
    }

    private void missingPathsMessage(final String driverPath) {
        List<String> missingPaths = getMissingPaths(driverPath);
        if (!missingPaths.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("There were missing paths in driverPath:").append(System.lineSeparator());
            for (String path : missingPaths) {
                sb.append("\t").append(path).append(System.lineSeparator());
            }
            sb.append("Use commandline option '-dp' to specify driver location.");
            this.logger.warn(sb.toString());
        }
    }

    /**
     * Returns a list of {@link File}s in <code>path</code> that do not exist.
     * The intent is to aid in diagnosing invalid paths.
     *
     * @param path
     * @return
     */
    private List<String> getMissingPaths(String path) {
        List<String> missingFiles = new ArrayList<>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            if (!new File(piece).exists())
                missingFiles.add(piece);
        }

        return missingFiles;
    }
}
