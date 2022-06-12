package org.schemaspy.output.html.mustache;

/**
 * Abstraction for the HTML elements used for displaying a diagram
 */

public interface Diagram {

    /**
     * @return html markup to display a diagram
     */
    String html();
}
