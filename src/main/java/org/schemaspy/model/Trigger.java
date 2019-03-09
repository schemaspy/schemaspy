package org.schemaspy.model;

/**
 * @author Ben Hartwich
 */
public class Trigger implements Comparable<Trigger> {
    private final String schema;
    private final String name;
    private final String actionStatement;
    private final String tableName;
    private final String actionTiming;
    private final String eventManipulation; // update or insert

    public Trigger(
            String schema,
            String name,
            String actionStatement,
            String tableName,
            String actionTiming,
            String eventManipulation
    ) {
        this.schema = schema;
        this.name = name;
        this.actionStatement = actionStatement;
        this.tableName = tableName;
        this.actionTiming = actionTiming;
        this.eventManipulation = eventManipulation;
    }

    /**
     * @return schema for the trigger name
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @return name of the trigger
     */
    public String getName() {
        return name;
    }

    /**
     * @return definition of the trigger
     */
    public String getActionStatement() {
        return actionStatement;
    }

    /**
     * @return name of the table the trigger belongs to
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return BEFORE or AFTER
     */
    public String getActionTiming() {
        return actionTiming;
    }

    /**
     * @return INSERT or UPDATE
     */
    public String getEventManipulation() {
        return eventManipulation;
    }

    public int compareTo(Trigger other) {
        int rc = getName().compareTo(other.getName());

        return rc;
    }

}
