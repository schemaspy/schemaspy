package org.schemaspy.util.markup;

import org.schemaspy.model.Table;

import java.util.Collection;
import java.util.HashMap;

public class PageRegistry {

    private final HashMap<String,String> registry = new HashMap<>();

    public PageRegistry register(Collection<Table> tables) {
        tables.forEach(this::registerTable);
        return this;
    }

    private void registerTable(Table table) {
        if (!table.isLogical()) {
            registry.put(table.getName(), new TablePath(table.getName()).value());
        }
    }

    public String pathForPage(String page) {
        return registry.get(page);
    }

}
