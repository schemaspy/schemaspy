package org.schemaspy.output;

import org.schemaspy.model.Database;

import java.io.File;

public interface OutputProducer {
    void generate(Database database, File outputDir);
}
