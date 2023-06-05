package org.schemaspy.util.markup;

import org.schemaspy.model.Table;
import org.schemaspy.util.naming.NameFromString;
import org.schemaspy.util.naming.SanitizedFileName;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public abstract class MarkupProcessor {
    protected final HashMap<String, String> pages = new HashMap<>();

    private static Optional<MarkupProcessor> instance = Optional.empty();

    public static MarkupProcessor getInstance() {
        if(!instance.isPresent()) {
            instance = Optional.of(new Markdown());
        }
        return instance.get();
    }

    public static void setInstance(MarkupProcessor instance) {
        MarkupProcessor.instance = Optional.of(instance);
    }

    public void registryPage(final Collection<Table> tables) {
        final String DOT_HTML = ".html";
        tables.stream()
                .filter(table -> !table.isLogical())
                .forEach(table -> {
                    String tablePath = "tables/" + new SanitizedFileName(new NameFromString(table.getName())).value() + DOT_HTML;
                    pages.put(table.getName(), tablePath);
                });
    }

    public String pagePath(String page) {
        return pages.get(page);
    }

    public String toHtml(final String markupText, final String rootPath) {
        if(markupText == null) {
            return null;
        }
        return parseToHtml(addReferenceLink(markupText, rootPath), rootPath).trim();
    }

    protected abstract String parseToHtml(final String markupText, final String rootPath);

    protected abstract String addReferenceLink(final String markupText, final String rootPath);
}
