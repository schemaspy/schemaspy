package org.schemaspy.model;

import org.schemaspy.util.naming.Name;

/**
 * Abstracts a collection of tables.
 */
public interface Tables {

    /**
     * Asks the collection for a specific member.
     * @param name The name of the table.
     * @return The table with the given name.
     */
    Table table(Name name);
}
