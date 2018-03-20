package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.schemaspy.cli.converters.CharsetConverter;
import org.schemaspy.cli.converters.SqlFormatterConverter;
import org.schemaspy.view.DefaultSqlFormatter;
import org.schemaspy.view.SqlFormatter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Parameters(resourceBundle = "htmlcommandlinearguments")
public class HtmlCommandLineArguments {

    @Parameter(
            names = {
                    "-charset",
                    "schemaspy.charset"
            },
            descriptionKey = "charset",
            converter = CharsetConverter.class
    )
    private Charset charset = StandardCharsets.UTF_8;

    @Parameter(
            names = {
                    "-nopages",
                    "schemaspy.nopages"
            },
            descriptionKey = "pagination"
    )
    private boolean paginationDisabled = false;
    @Parameter(
            names = {
                    "-ahic",
                    "schemaspy.ahic"
            },
            descriptionKey = "encodedComments"
    )
    private boolean encodedCommentsDisabled = false;
    @Parameter(
            names = {
                    "-norows",
                    "schemaspy.norows"
            },
            descriptionKey = "numRows"
    )
    private boolean numRowsDisabled = false;
    @Parameter(
            names = {
                    "-desc",
                    "schemaspy.desc"
            },
            descriptionKey = "description"
    )
    private String description;
    @Parameter(
            names = {
                    "-template",
                    "schemaspy.template"
            },
            descriptionKey = "template"
    )
    private String templateDirectory = "/layout";
    @Parameter(
            names = {
                    "-css",
                    "schemaspy.css"
            },
            descriptionKey = "css"
    )
    private String cssFile = "schemaSpy.css";
    @Parameter(
            names = {
                    "-font",
                    "schemaspy.font"
            },
            descriptionKey = "font"
    )
    private String font = "Helvetica";
    @Parameter(
            names = {
                    "-fontsize",
                    "schemaspy.fontsize"
            },
            descriptionKey = "fontSize"
    )
    private Integer fontSize = 11;

    @Parameter(
            names = {
                    "-sqlFormatter",
                    "schemaspy.sqlformatter"
            },
            descriptionKey = "sqlFormatter",
            converter = SqlFormatterConverter.class
    )
    private SqlFormatter sqlFormatter = new DefaultSqlFormatter();

    @ParametersDelegate
    private DotCommandLineArguments dotCommandLineArguments = new DotCommandLineArguments();

    public Charset getCharset() {
        return charset;
    }

    public boolean paginationEnabled() {
        return !paginationDisabled;
    }

    public boolean paginationDisabled() {
        return paginationDisabled;
    }

    public boolean encodedCommentsEnabled() {
        return !encodedCommentsDisabled;
    }

    public boolean encodedCommentsDisabled() {
        return encodedCommentsDisabled;
    }

    public boolean numberOfRowsEnabled() {
        return !numRowsDisabled;
    }

    public boolean numberOfRowsDisabled() {
        return numRowsDisabled;
    }

    public String getDescription() {
        return description;
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public String getCssFile() {
        return cssFile;
    }

    public String getFont() {
        return font;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public SqlFormatter getSqlFormatter() {
        return sqlFormatter;
    }

    public DotCommandLineArguments getDotCommandLineArguments() {
        return dotCommandLineArguments;
    }
}
