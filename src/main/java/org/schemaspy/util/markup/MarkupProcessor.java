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
        return parseToHtml(new WithReferenceLinks(pageRegistry, markupText, rootPath, getLinkFormat()).value()).trim();
    }

    protected abstract String parseToHtml(final String markupText);

    /**
     * Expecting a format following {@link java.util.Formatter} that accepts PageLink and PagePath.
     *
     * @return string format that accept 2 arguments. (1) PageLink, (2) PagePath
     */
    protected abstract String getLinkFormat();
}
