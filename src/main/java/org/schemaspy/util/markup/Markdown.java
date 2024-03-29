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

/**
 * Created by rkasa on 2016-04-11.
 *
 * @author Rafal Kasa
 * @author Daniel Watt
 * @author Samuel Dussault
 */
public class Markdown implements Markup {

    public static final String LINK_FORMAT = "[%1$s](%2$s)";
    private final Parser parser;
    private final HtmlRenderer renderer;
    private final Markup origin;

    public Markdown(
        final Markup origin
    ) {
        this(
            origin,
            PegdownOptionsAdapter.flexmarkOptions(true,
                    Extensions.ALL ^ Extensions.HARDWRAPS
            )
        );
    }

    public Markdown(
        final Markup origin,
        final DataHolder options
    ) {
        this(
            origin,
            Parser.builder(options).build(),
            HtmlRenderer.builder(options).build()
        );
    }

    public Markdown(
        final Markup origin,
        final Parser parser,
        final HtmlRenderer renderer
    ) {
        this.origin = origin;
        this.parser = parser;
        this.renderer = renderer;
    }

    @Override
    public String value() {
        return renderer.render(
            parser.parse(
                origin.value()
            )
        ).trim();
    }
}
