package org.schemaspy.output.dot.schemaspy.connectors;

import java.util.Set;
import org.schemaspy.output.dot.schemaspy.DotConnector;

/**
 * Format table data into .dot format to feed to Graphvis' dot program.
 *
 * @author John Currier
 */
public interface DotConnectors {
    Set<DotConnector> unique();
}
