package org.schemaspy.util.markup;

import org.schemaspy.util.naming.Concatenation;
import org.schemaspy.util.naming.Name;
import org.schemaspy.util.naming.NameFromString;
import org.schemaspy.util.naming.SanitizedFileName;

public class TablePath {

    private final Name value;

    public TablePath(String tableName) {
        this.value =
                new Concatenation(
                    new Concatenation(
                        new NameFromString("tables/"),
                        new SanitizedFileName(
                            new NameFromString(tableName)
                        )
                    ),
                    new NameFromString(".html")
                );
    }

    public String value() {
        return value.value();
    }
}
