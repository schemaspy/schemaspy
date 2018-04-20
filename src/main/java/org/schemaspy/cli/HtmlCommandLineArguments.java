package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.schemaspy.cli.converters.CharsetConverter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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
                    "-columndetails",
                    "schemaspy.columndetails"
            },
            descriptionKey = "columnDetails"
    )
    private List<String> columnDetails = Arrays.asList("id", "table", "column", "type", "size", "nulls", "auto", "default");
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

    public List<String> getColumnDetails() {
        return columnDetails;
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

    public DotCommandLineArguments getDotCommandLineArguments() {
        return dotCommandLineArguments;
    }
}
