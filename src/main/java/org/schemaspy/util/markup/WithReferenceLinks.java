package org.schemaspy.util.markup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WithReferenceLinks implements Markup {

    private final PageRegistry pageRegistry;
    private final Markup origin;
    private final String rootPath;
    private final String linkFormat;

    /**
     * Adds pages reference links to the given markup text. Links in the format [page] or [page.anchor]
     * are replaced with reference-style links, for example: [page.anchor](./pagePath#anchor) in Markdown.
     * The page can be registered using {@link org.schemaspy.view.HtmlConfig#registryPage} method.
     *
     * @param pageRegistry The PageRegistry used to register pages
     * @param origin The markup text to which page links will be added.
     * @param rootPath The root path used for constructing the page paths (defaults to ".").
     * @param linkFormat The formatting string for a link in markup
     */
    public WithReferenceLinks(Markup origin, PageRegistry pageRegistry, String rootPath, String linkFormat) {
        this.origin = origin;
        this.pageRegistry = pageRegistry;
        this.rootPath = rootPath;
        this.linkFormat = linkFormat;
    }

    public String value() {
        String originValue = origin.value();
        String markupTextWithReferenceLink = originValue;
        String basePath = (rootPath == null || rootPath.isEmpty()) ? "." : rootPath;

        Pattern p = Pattern.compile("\\[(.*?)]");
        Matcher m = p.matcher(originValue);

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
