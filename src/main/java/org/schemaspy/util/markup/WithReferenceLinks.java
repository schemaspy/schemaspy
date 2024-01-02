package org.schemaspy.util.markup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WithReferenceLinks {

    private final PageRegistry pageRegistry;
    private final String markupText;
    private final String rootPath;
    private final String linkFormat;

    /**
     * Adds pages reference links to the given markup text. Links in the format [page] or [page.anchor]
     * are replaced with reference-style links, for example: [page.anchor](./pagePath#anchor) in Markdown.
     * The page paths are obtained from the registry page registered using the {@link MarkupProcessor#registryPage} method.
     *
     * @param pageRegistry The PageRegistry used to register pages
     * @param markupText The markup text to which page links will be added.
     * @param rootPath The root path used for constructing the page paths (defaults to ".").
     * @param linkFormat The formatting string for a link in markup
     */
    public WithReferenceLinks(PageRegistry pageRegistry, String markupText, String rootPath, String linkFormat) {
        this.pageRegistry = pageRegistry;
        this.markupText = markupText;
        this.rootPath = rootPath;
        this.linkFormat = linkFormat;
    }

    public String value() {
        String markupTextWithReferenceLink = markupText;
        String basePath = (rootPath == null || rootPath.isEmpty()) ? "." : rootPath;

        Pattern p = Pattern.compile("\\[(.*?)]");
        Matcher m = p.matcher(markupText);

        while (m.find()) {
            String pageLink = m.group(1);
            String tableName = pageLink;

            String anchorLink = "";
            int anchorPosition = pageLink.lastIndexOf('.');

            if (anchorPosition > -1) {
                anchorLink = pageLink.substring(anchorPosition + 1).trim();
                tableName = pageLink.substring(0, anchorPosition);
            }

            String pagePath = pageRegistry.pathForPage(tableName);
            if (pagePath != null) {
                pagePath = String.format("%s/%s", basePath, pagePath);
                if (!anchorLink.isEmpty()) {
                    pagePath += "#" + anchorLink;
                }

                markupTextWithReferenceLink = markupTextWithReferenceLink.replace(String.format("[%s]", pageLink), linkFormat.formatted(pageLink, pagePath));
            }
        }

        return markupTextWithReferenceLink;
    }
}
