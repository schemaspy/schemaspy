/*
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

import org.junit.Ignore;
import org.junit.Test;
import org.schemaspy.model.Trigger;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlTriggersPageTest {

    @Test
    @Ignore
    public void triggerPage() {
        //TODO: confirm how to use similar code to test generation of this page
        HtmlConfig htmlConfig = mock(HtmlConfig.class);
        when(htmlConfig.getTemplateDirectory()).thenReturn("layout");
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", htmlConfig);
        HtmlTriggersPage htmlTriggersPage= new HtmlTriggersPage(mustacheCompiler);
        Collection<Trigger> triggers = Collections.singletonList(new Trigger("myDb",
                "myTrigger",
                "BEGIN\n" +
                        "        SET NEW.updatedOnDTime = NOW();\n" +
                        "    END",
                "myTable",
                "AFTER",
                "INSERT"));
        StringWriter actual = new StringWriter();

        htmlTriggersPage.write(triggers, actual);
    }

}