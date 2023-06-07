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
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by samdus on 2023-06-05
 *
 * @author Samuel Dussault
 */
public class Asciidoc extends MarkupProcessor {

    @Override
    protected String parseToHtml(String markupText) {
        try(Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
            return asciidoctor.convert(markupText, Options.builder().build());
        }
    }

    @Override
    protected String addReferenceLink(String markupText, String rootPath) {
        if(rootPath == null || rootPath.length() == 0) {
            return markupText;
        }

        final String regex = "xref:(.+)(\\[.+])";
        final String subst = String.format("xref:./%s/$1$2", rootPath);

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(markupText);

        return matcher.replaceAll(subst);
    }
}
