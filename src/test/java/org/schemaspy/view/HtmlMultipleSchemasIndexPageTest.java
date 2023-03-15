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

import org.junit.BeforeClass;
import org.junit.Test;
import org.schemaspy.cli.CommandLineArgumentParser;
import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.model.Catalog;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlMultipleSchemasIndexPageTest {

    private static CommandLineArguments commandLineArguments;
    private static DataTableConfig dataTableConfig;

    @BeforeClass
    public static void setup() {
        commandLineArguments = parse("");
        dataTableConfig = new DataTableConfig(commandLineArguments);
    }

    private static CommandLineArguments parse(String...args) {
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

    @Test
    public void multiMainIndexShouldHaveDescription() {
        MustacheCompiler mustacheCompiler = new MustacheCompiler("withComment", null, commandLineArguments.getHtmlConfig(), false, dataTableConfig);
        HtmlMultipleSchemasIndexPage htmlMultipleSchemasIndexPage = new HtmlMultipleSchemasIndexPage(mustacheCompiler);
        StringWriter actual = new StringWriter();
        htmlMultipleSchemasIndexPage.write(new MustacheCatalog(new Catalog("dbo"),""), Collections.emptyList(),"A Description", "JAVA_TEST 1.0", actual);
        assertThat(actual.toString()).contains("<p>A Description</p>");
    }

    @Test
    public void multiMainIndexShouldNOTHaveDescription() {
        MustacheCompiler mustacheCompiler = new MustacheCompiler("noComment", null, commandLineArguments.getHtmlConfig(), false, dataTableConfig);
        HtmlMultipleSchemasIndexPage htmlMultipleSchemasIndexPage = new HtmlMultipleSchemasIndexPage(mustacheCompiler);
        StringWriter actual = new StringWriter();
        htmlMultipleSchemasIndexPage.write(new MustacheCatalog(new Catalog("dbo"),""), Collections.emptyList(),null, "JAVA_TEST 1.0", actual);
        assertThat(actual.toString()).doesNotContain("<p>A Description</p>");
    }

}