package org.schemaspy.cli;

import org.schemaspy.model.Database;
import org.schemaspy.model.Table;
import org.schemaspy.view.SqlFormatter;

import java.util.Set;

public class DummySqlFormatter implements SqlFormatter{
    @Override
    public String format(String sql, Database db, Set<Table> references) {
        return null;
    }
}
