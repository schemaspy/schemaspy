package org.schemaspy.util.filefilter;

import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

public class NotHtml implements FileFilter {

    private final IOFileFilter origin;
    private static final String DOT_HTML = ".html";

    public NotHtml() {
        this(FileFilterUtils
            .and(
                FileFilterUtils
                    .notFileFilter(
                        FileFilterUtils.suffixFileFilter(DOT_HTML)
                    )
            )
        );
    }

    public NotHtml(final IOFileFilter origin) {
        this.origin = origin;
    }

    @Override
    public boolean accept(final File pathname) {
        return this.origin.accept(pathname);
    }
}
