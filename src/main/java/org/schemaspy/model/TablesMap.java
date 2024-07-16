package org.schemaspy.model;

import java.util.Map;
import org.schemaspy.util.naming.Name;

/**
 * Encapsulates a collection of tables by key-value pairs.
 */
public class TablesMap implements Tables {

    private final Map<String, Table> map;

    public TablesMap(final Map<String, Table> map) {
        this.map = map;
    }

    @Override
    public Table table(final Name name) {
        final String strName = name.value();
        return !strName.isEmpty() ? this.map.get(strName) : null;
    }
}
