package org.schemaspy.output.html;

import org.schemaspy.output.OutputException;

/**
 * Runtime exception for usage within HTML creation
 */
public class HtmlException extends OutputException {


    public HtmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
