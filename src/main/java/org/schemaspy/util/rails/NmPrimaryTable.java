package org.schemaspy.util.rails;

import org.schemaspy.util.Inflection;
import org.schemaspy.util.naming.Name;

/**
 * Name of the primary table name, in rails naming convention.
 */
public class NmPrimaryTable implements Name {

    private final String column;

    public NmPrimaryTable(final String column) {
        this.column = column;
    }

    @Override
    public String value() {
        final String lowered = this.column.toLowerCase();
        if (lowered.endsWith("_id")) {
            String singular = lowered.substring(0, lowered.length() - "_id".length());
            String primaryTableName = Inflection.pluralize(singular);
            return primaryTableName;
        } else {
            return "";
        }
    }
}
