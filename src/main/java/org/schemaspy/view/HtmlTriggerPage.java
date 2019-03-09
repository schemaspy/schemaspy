/*
 * Copyright (C) 2017 Daniel Watt
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.view;

import org.schemaspy.model.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;

/**
 * @author Ben Hartwich (templated from HtmlRoutinePage)
 */
public class HtmlTriggerPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MustacheCompiler mustacheCompiler;

    public HtmlTriggerPage(MustacheCompiler mustacheCompiler) {
        this.mustacheCompiler = mustacheCompiler;
    }

    public void write(Trigger trigger, Writer writer) {
        PageData pageData = new PageData.Builder()
                .templateName("triggers/trigger.html")
                .scriptName("trigger.js")
                .addToScope("triggerName", trigger.getName())
                .addToScope("triggerDefinition",trigger.getActionStatement())
                .addToScope("triggerTable", trigger.getTableName())
                .addToScope("triggerEvent", trigger.getEventManipulation())
                .addToScope("triggerTiming", trigger.getActionTiming())
                .depth(1)
                .getPageData();
        try {
            mustacheCompiler.write(pageData, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to write trigger page for '{}'", trigger.getName(), e);
        }
    }
}
