/*
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

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;

/**
 * Created by samdus on 2023-06-05
 *
 * @author Samuel Dussault
 */
public class Asciidoc implements Markup {

    private final PageRegistry pageRegistry;
    private final String markupText;
    private final String rootPath;

    public Asciidoc(
        final PageRegistry pageRegistry,
        final String markupText,
        final String rootPath
    ) {
        this.pageRegistry = pageRegistry;
        this.markupText = markupText;
        this.rootPath = rootPath;
    }

    @Override
    public String toHtml() {
        try(Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            return asciidoctor
                .convert(
                    new WithReferenceLinks(pageRegistry, markupText, rootPath, getLinkFormat()).value(),
                    Options.builder().build()
                );
        }
    }

    protected String getLinkFormat() {
        return "link:%2$s[%1$s]";
    }
}
