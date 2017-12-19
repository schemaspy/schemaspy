package org.schemaspy.util;

import java.io.IOException;

public class DotFailure extends IOException {
    private static final long serialVersionUID = 3833743270181351987L;

    public DotFailure(String msg) {
        super(msg);
    }
}
