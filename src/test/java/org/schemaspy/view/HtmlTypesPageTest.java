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
import org.schemaspy.model.Type;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlTypesPageTest {

    @Test
    public void typesMarkdownDescription() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("markdownTest", "markdownTest", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlTypesPage htmlRoutinesPage = new HtmlTypesPage(mustacheCompiler);
        Collection<Type> types = Collections.singletonList(new Type(
                "Domain",
                null,
                "type_tests",
                "test_domain",
                "Description for domain **type_tests.test_domain**",
                "integer NOT NULL"));
        StringWriter actual = new StringWriter();

        htmlRoutinesPage.write(types, actual);

        assertThat(actual.toString()).contains("<td><p>Description for domain <strong>type_tests.test_domain</strong></p></td>");
    }

    private CommandLineArguments parse(String... args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
                new CommandLineArguments(),
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