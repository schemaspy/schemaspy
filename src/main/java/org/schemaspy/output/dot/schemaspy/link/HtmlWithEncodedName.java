package org.schemaspy.output.dot.schemaspy.link;

import org.schemaspy.model.Table;
import org.schemaspy.view.FileNameGenerator;

public class HtmlWithEncodedName implements NodeLink {

    private final Table table;
    public HtmlWithEncodedName(Table table) {
        this.table = table;
    }

    @Override
    public String asString() {
        return new FileNameGenerator().generate(table.getName()) + ".html";
    }

}
