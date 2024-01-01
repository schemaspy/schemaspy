/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2023 Samuel Dussault
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.util.markup;

import org.schemaspy.model.Table;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracted from Markdown class by samdus on 2023-06-05
 *
 * @author Rafal Kasa
 * @author Daniel Watt
 * @author Samuel Dussault
 */
public abstract class MarkupProcessor {

    protected final PageRegistry pageRegistry = new PageRegistry();

    private static Optional<MarkupProcessor> instance = Optional.empty();

    public static MarkupProcessor getInstance() {
        if (!instance.isPresent()) {
            instance = Optional.of(new Markdown());
        }
        return instance.get();
    }

    public static void setInstance(MarkupProcessor instance) {
        MarkupProcessor.instance = Optional.of(instance);
    }

    public void registryPage(final Collection<Table> tables) {
        pageRegistry.register(tables);
    }

    public String toHtml(final String markupText, final String rootPath) {
        if (markupText == null) {
            return null;
        }
        return parseToHtml(addReferenceLink(markupText, rootPath)).trim();
    }

    /**
     * Adds pages reference links to the given markup text. Links in the format [page] or [page.anchor]
     * are replaced with reference-style links, for example: [page.anchor](./pagePath#anchor) in Markdown.
     * The page paths are obtained from the registry page registered using the {@link #registryPage} method.
     *
     * @param markupText The markup text to which page links will be added.
     * @param rootPathArg The root path used for constructing the page paths (defaults to ".").
     * @return The modified markup text with added reference links.
     */
    protected String addReferenceLink(final String markupText, final String rootPathArg) {
        //TODO: Use the rootPath and add a test for it
        String markupTextWithReferenceLink = markupText;
        String rootPath = (rootPathArg == null || rootPathArg.isEmpty()) ? "." : rootPathArg;

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
                pagePath = String.format("%s/%s", rootPath, pagePath);
                if (!"".equals(anchorLink)) {
                    pagePath += "#" + anchorLink;
                }

                markupTextWithReferenceLink = markupTextWithReferenceLink.replace(String.format("[%s]", pageLink), formatLink(pageLink, pagePath));
            }
        }

        return markupTextWithReferenceLink;
    }

    protected abstract String parseToHtml(final String markupText);

    protected abstract String formatLink(final String pageName, final String pagePath);
}
