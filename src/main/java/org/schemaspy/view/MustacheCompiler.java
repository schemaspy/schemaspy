package org.schemaspy.view;

import java.io.IOException;
import java.io.Writer;

public interface MustacheCompiler {
    void write(PageData pageData, Writer writer) throws IOException;

    String getRootPath(int depth);
}
