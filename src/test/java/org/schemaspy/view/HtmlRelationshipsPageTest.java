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
import org.schemaspy.output.OutputException;
import org.schemaspy.output.html.mustache.diagrams.MustacheSummaryDiagramResults;
import org.schemaspy.util.DataTableConfig;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlRelationshipsPageTest {

    @Test
    public void willWriteErrorInformation() {
        CommandLineArguments arguments = parse("");
        DataTableConfig dataTableConfig = new DataTableConfig(arguments);
        MustacheCompiler mustacheCompiler = new MustacheCompiler("htmlTablePage_error", "htmlTablePage_error", arguments.getHtmlConfig(), false, dataTableConfig);
        HtmlRelationshipsPage htmlRelationshipsPage = new HtmlRelationshipsPage(mustacheCompiler,  true,false);
        StringWriter writer = new StringWriter();
        MustacheSummaryDiagramResults mustacheSummaryDiagramResults = new MustacheSummaryDiagramResults(Collections.emptyList(), Collections.singletonList(new OutputException("ERROR")));

        htmlRelationshipsPage.write(mustacheSummaryDiagramResults, writer);

        assertThat(writer.toString()).contains("here were 1 error(s)");
    }

    private CommandLineArguments parse(String...args) {
        String[] defaultArgs = {"-o", "out", "-sso"};
        return new CommandLineArgumentParser(
                Stream
                        .concat(
                                Arrays.stream(defaultArgs),
                                Arrays.stream(args)
                        ).toArray(String[]::new)
        )
                .commandLineArguments();
    }
}