package org.schemaspy.input.dbms.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class LoadAdditionalJarsForDriver implements Classpath {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String driverPath;

    public LoadAdditionalJarsForDriver(final String driverPath) {
        this.driverPath = driverPath;
    }

    @Override
    public Set<URI> paths() {
        Set<URI> result = new HashSet<>();
        File driverFolder = new File(Paths.get(this.driverPath).getParent().toString());
        if (driverFolder.exists()) {
            File[] files = driverFolder.listFiles(
                    (dir, name) -> name.toLowerCase().matches(".*\\.?ar$")
            );

            LOGGER.info("Additional files will be loaded for JDBC Driver");

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        result.add(file.toURI());
                        LOGGER.info("Added: {}", file.toURI());
                    }
                }
            }
        }
        return result;
    }
}
