/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Daniel Watt
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

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.options.DataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-04-11.
 *
 * @author Rafal Kasa
 * @author Daniel Watt
 */
public class Markdown extends MarkupProcessor {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public Markdown() {
        this(
                PegdownOptionsAdapter.flexmarkOptions(true,
                        Extensions.ALL ^ Extensions.HARDWRAPS
                )
        );
    }

    public Markdown(final DataHolder options) {
        this(
                Parser.builder(options).build(),
                HtmlRenderer.builder(options).build()
        );
    }

    public Markdown(
            final Parser parser,
            final HtmlRenderer renderer
    ) {
        this.parser = parser;
        this.renderer = renderer;
    }

    @Override
    protected String parseToHtml(final String markupText, final String rootPath) {
        if (markupText == null) {
            return null;
        }

        return renderer.render(
            parser.parse(
                markupText
            )
        ).trim();
    }



    @Override
    protected String addReferenceLink(final String markupText, final String rootPath) {
        StringBuilder text = new StringBuilder(markupText);
        String newLine = "\r\n";

        Pattern p = Pattern.compile("\\[(.*?)]");
        Matcher m = p.matcher(markupText);

        List<String> links = new ArrayList<>();

        while(m.find()) {
            links.add(m.group(1));
        }

        if (!links.isEmpty()) {
            text.append(newLine).append(newLine);
        }

        for (String link : links) {
            String anchorLink = "";
            String pageLink = link;
            int anchorPosition = link.lastIndexOf('.');

            if (anchorPosition > -1) {
                anchorLink = link.substring(anchorPosition + 1).trim();
                pageLink = link.substring(0, anchorPosition);
            }

            String path = rootPath+pagePath(pageLink);
            if (!"".equals(anchorLink)) {
                path = path + "#" + anchorLink;
            }
            text.append("[").append(link).append("]: ./").append(path).append(newLine);
        }

        return text.toString();
    }
}
