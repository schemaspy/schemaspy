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

import org.junit.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Routine;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlRoutinesPageTest {

    @Test
    public void markdownComment() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", "markdownTest", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler);
        Collection<Routine> routines = Collections.singletonList(new Routine("ARoutine", "Function", "Integer", "SQL", "SELECT 1", true, "IMMUTABLE", "INVOKER", "normal *emp* **strong**"));
        StringWriter actual = new StringWriter();

        htmlRoutinesPage.write(routines, actual);

        assertThat(actual.toString()).contains("<p>normal <em>emp</em> <strong>strong</strong></p>");
    }

    @Test
    public void rawValueToFileName() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", "markdownTest", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlRoutinesPage htmlRoutinesPage = new HtmlRoutinesPage(mustacheCompiler);
        Collection<Routine> routines = Collections.singletonList(new Routine("demofn(arg1 text, arg2 mytype DEFAULT 'one'::mytype)", "Function", "Integer", "SQL", "SELECT 1", true, "IMMUTABLE", "INVOKER", "normal *emp* **strong**"));
        StringWriter actual = new StringWriter();

        htmlRoutinesPage.write(routines, actual);

        assertThat(actual.toString()).contains("demofn_arg1_text__arg2_mytype_D_2c10faf9");
    }

    private CommandLineArguments parse(String...args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
            (option) -> null
        )
            .parse(
                Stream
                    .concat(
                        Arrays.stream(defaultArgs),
                        Arrays.stream(args)
                    ).toArray(String[]::new));
    }
}