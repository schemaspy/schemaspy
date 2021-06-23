package org.schemaspy.output.dot.schemaspy.relatives;

public interface ExclusionFilter {

    Iterable<Verdict> children();

    Iterable<Verdict> parents();
}
