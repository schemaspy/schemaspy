package org.schemaspy.cli;

import com.beust.jcommander.JCommander;
import org.junit.Rule;
import org.junit.Test;
import org.schemaspy.cli.converters.CharsetConverter;
import org.schemaspy.cli.converters.SqlFormatterConverter;
import org.schemaspy.testing.Logger;
import org.schemaspy.testing.LoggingRule;
import org.schemaspy.view.DefaultSqlFormatter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlCommandLineArgumentsTest {

    @Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Test
    public void charsetDefault() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse();
        assertThat(htmlCommandLineArguments.getCharset().name()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    public void charsetExists() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse("-charset", "UTF-16");
        assertThat(htmlCommandLineArguments.getCharset().name()).isEqualTo(StandardCharsets.UTF_16.name());
    }

    @Test
    @Logger(CharsetConverter.class)
    public void charsetDoesNotExists() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse("-charset", "UTF-2");
        assertThat(htmlCommandLineArguments.getCharset().name()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(loggingRule.getLog()).contains("falling back to UTF-8");
    }

    @Test
    public void sqlFormatterDefault() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse();
        assertThat(
                htmlCommandLineArguments.getSqlFormatter().getClass().getName()
        ).isEqualTo(
                DefaultSqlFormatter.class.getName()
        );
    }

    @Test
    public void sqlFormatterDummyExists() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse("-sqlFormatter", DummySqlFormatter.class.getName());
        assertThat(
                htmlCommandLineArguments.getSqlFormatter().getClass().getName()
        ).isEqualTo(
                DummySqlFormatter.class.getName()
        );
    }

    @Test
    @Logger(SqlFormatterConverter.class)
    public void sqlFormatterDoesNotExist() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse("-sqlFormatter", "this.class.does.not.exist");
        assertThat(
                htmlCommandLineArguments.getSqlFormatter().getClass().getName()
        ).isEqualTo(
                DefaultSqlFormatter.class.getName()
        );
        assertThat(loggingRule.getLog()).contains("falling back to DefaultSqlFormatter");
    }

    @Test
    public void columnDetailsDefault() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse();
        assertThat(htmlCommandLineArguments.getColumnDetails())
                .containsExactly("id", "table", "column", "type", "size", "nulls", "auto", "default");

    }

    @Test
    public void columnDetailsCustom() {
        HtmlCommandLineArguments htmlCommandLineArguments = new HtmlCommandLineArguments();
        new JCommander(htmlCommandLineArguments).parse("-columndetails", "id,table,column,type");
        assertThat(htmlCommandLineArguments.getColumnDetails().size()).isEqualTo(4);
        assertThat(htmlCommandLineArguments.getColumnDetails())
                .containsExactly("id", "table", "column", "type");
    }


}