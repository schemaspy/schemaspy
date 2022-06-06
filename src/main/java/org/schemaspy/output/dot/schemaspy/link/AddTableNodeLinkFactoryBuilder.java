package org.schemaspy.output.dot.schemaspy.link;

public class AddTableNodeLinkFactoryBuilder {
    private final boolean multiSchema;

    public AddTableNodeLinkFactoryBuilder(boolean multiSchema) {
        this.multiSchema = multiSchema;
    }

    public TableNodeLinkFactory withTableNodeLinkFactory(TableNodeLinkFactory tableNodeLinkFactory) {
        return new AddTableNodeLinkFactory(multiSchema, tableNodeLinkFactory);
    }
}
