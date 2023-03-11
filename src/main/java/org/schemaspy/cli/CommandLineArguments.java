/*
 * Copyright (C) 2017 Thomas Traude
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
package org.schemaspy.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.schemaspy.output.diagram.graphviz.GraphvizConfig;
import org.schemaspy.output.diagram.graphviz.GraphvizConfigCli;
import org.schemaspy.view.HtmlConfig;
import org.schemaspy.view.HtmlConfigCli;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds all supported command line arguments.
 * <p>
 * An instance of this class registered as singleton bean in {@link org.schemaspy.SchemaSpyConfiguration} via {@link CommandLineArgumentParser}.
 * Therefore it can be injected in other beans, for example:
 * <p>
 * <pre>
 *     public class MyService {
 *         private CommandLineArguments arguments;
 *
 *         // use constructor injection
 *         public MyService(CommandLineArguments arguments) {
 *             this.arguments = arguments
 *         }
 *     }
 * </pre>
 * <p>
 * TODO migrate other command line parameter from {@link org.schemaspy.Config}
 *
 * @author Thomas Traude
 * @author Nils Petzaell
 */
@Parameters(resourceBundle = "commandlinearguments")
public class CommandLineArguments {

    @Parameter(names = {
        "?", "-?", "/?",
        "-h",
        "-help", "--help"
    },
        descriptionKey = "help",
        help = true,
        order = 1
    )
    private boolean helpRequired;

    @Parameter(
        names = {
            "-dbHelp", "-dbhelp",
            "--dbHelp", "--dbhelp"
        },
        help = true,
        descriptionKey = "dbhelp",
        order = 2
    )
    private boolean dbHelpRequired;

    @Parameter(
        names = {
            "-l", "--license"
        },
        help = true,
        descriptionKey = "license",
        order = 3
    )
    private boolean printLicense;

    @Parameter(
        names = {
            "-debug", "--debug",
            "schemaspy.debug"
        },
        descriptionKey = "debug"
    )
    private boolean debug = false;

    @Parameter(
        names = {
            "-nohtml", "--no-html",
            "schemaspy.nohtml"
        },
        descriptionKey = "nohtml"
    )
    private boolean nohtml = false;

    @Parameter(
        names = {
            "-noimplied", "--no-implied",
            "schemaspy.noimplied"
        },
        descriptionKey = "noimplied"
    )
    private boolean noImplied = false;

    @Parameter(
        names = {
            "-t", "--database-type",
            "schemaspy.t", "schemaspy.database-type"
        },
        descriptionKey = "database-type"
    )
    private String databaseType = "ora";

    @Parameter(
        names = {
            "-connprops",
            "schemaspy.connprops"
        },
        descriptionKey = "connprops"
    )
    private String connprops = null;

    @Parameter(
        names = {
            "-db", "--database-name",
            "schemaspy.db", "schemaspy.database-name"
        },
        descriptionKey = "databaseName"
    )
    private String databaseName;

    @Parameter(
        names = {
            "-meta", "--schema-meta",
            "schemaspy.meta"
        },
        descriptionKey = "meta"
    )
    private String schemaMeta;

    @Parameter(
        names = {
            "-sso", "--single-sign-on",
            "schemaspy.sso", "schemaspy.single-sign-on"
        },
        descriptionKey = "sso"
    )
    private boolean sso = false;

    @Parameter(
        names = {
            "-u", "--user",
            "schemaspy.u", "schemaspy.user"},
        descriptionKey = "user"
    )
    private String user;

    @Parameter(
        names = {
            "-p", "--password",
            "schemaspy.pw", "schemaspy.password"
        },
        descriptionKey = "password"
    )
    private String password;

    @Parameter(
        names = {
            "-pfp", "--prompt-for-password",
            "schemaspy.pfp"
        },
        descriptionKey = "pfp",
        password = true
    )
    private String passwordFromPrompt;

    @Parameter(
        names = {
            "-s", "--schema",
            "schemaspy.s", "schemaspy.schema"

        },
        descriptionKey = "schema"
    )
    private String schema;

    @Parameter(
        names = {
            "-cat", "--catalog",
            "schemaspy.cat", "schemaspy.catalog"
        },
        descriptionKey = "catalog"
    )
    private String catalog;

    @Parameter(
        names = {
            "-dp", "--driverPath",
            "schemaspy.dp", "schemaspy.driverPath"
        },
        descriptionKey = "driverPath"
    )
    private String driverPath;

    @Parameter(
        names = {
            "-o", "--outputDirectory",
            "schemaspy.o", "schemaspy.outputDirectory"
        },
        descriptionKey = "outputDirectory"
    )
    private File outputDirectory;

    @Parameter(
        names = {
            "-host", "--host",
            "schemaspy.host"
        },
        descriptionKey = "host"
    )
    private String host;

    @Parameter(
        names = {
            "-port", "--port",
            "schemaspy.port"
        },
        descriptionKey = "port"
    )
    private Integer port;

    @ParametersDelegate
    private GraphvizConfigCli graphvizConfig = new GraphvizConfigCli();

    @ParametersDelegate
    private NoRowsConfigCli noRowsConfigCli = new NoRowsConfigCli();

    @ParametersDelegate
    private TemplateDirectoryConfigCli templateDirectoryConfigCli = new TemplateDirectoryConfigCli();

    @ParametersDelegate
    private HtmlConfigCli htmlConfigCli = new HtmlConfigCli(noRowsConfigCli, templateDirectoryConfigCli);

    @Parameter(
        names = {
            "-vizjs",
            "schemaspy.vizjs"
        },
        descriptionKey = "vizjs",
        required = false
    )
    private boolean useVizJS;

    @Parameter(
        names = {
            "-degree",
            "schemaspy.degree"
        },
        descriptionKey = "degree",
        required = false,
        validateValueWith = DegreeOfSeparationValidator.class
    )
    private int degreeOfSeparation = 2;


    // DataTables options for database_objects (Tables) table in src/main/resources/layout/main.html
    @Parameter(
        names = {
            "-noDbObjectPaging", "--noDbObjectPaging",
            "schemaspy.noDbObjectPaging"
        },
        descriptionKey = "noDbObjectPaging"
    )
    private boolean noDbObjectPaging = false;

    @Parameter(
        names = {
            "-dbObjectPageLength", "--dbObjectPageLength",
            "schemaspy.dbObjectPageLength"
        },
        descriptionKey = "dbObjectPageLength"
    )
    private int dbObjectPageLength = 50;

    @Parameter(
        names = {
            "-dbObjectLengthChange", "--dbObjectLengthChange",
            "schemaspy.dbObjectLengthChange"
        },
        descriptionKey = "dbObjectLengthChange"
    )
    private boolean dbObjectLengthChange = false;


    // DataTables options for standard_table (Columns) table in src/main/resources/layout/tables/table.html
    @Parameter(
        names = {
            "-noTablePaging", "--noTablePaging",
            "schemaspy.noTablePaging"
        },
        descriptionKey = "noTablePaging"
    )
    private boolean noTablePaging = false;

    @Parameter(
        names = {
            "-tablePageLength", "--tablePageLength",
            "schemaspy.tablePageLength"
        },
        descriptionKey = "tablePageLength"
    )
    private int tablePageLength = 10;

    @Parameter(
        names = {
            "-tableLengthChange", "--tableLengthChange",
            "schemaspy.tableLengthChange"
        },
        descriptionKey = "tableLengthChange"
    )
    private boolean tableLengthChange = false;


    // DataTables options for indexes_table (Indexes) table in src/main/resources/layout/tables/table.html
    @Parameter(
        names = {
            "-noIndexPaging", "--noIndexPaging",
            "schemaspy.noIndexPaging"
        },
        descriptionKey = "noIndexPaging"
    )
    private boolean noIndexPaging = false;

    @Parameter(
        names = {
            "-indexPageLength", "--indexPageLength",
            "schemaspy.indexPageLength"
        },
        descriptionKey = "indexPageLength"
    )
    private int indexPageLength = 10;

    @Parameter(
        names = {
            "-indexLengthChange", "--indexLengthChange",
            "schemaspy.indexLengthChange"
        },
        descriptionKey = "indexLengthChange"
    )
    private boolean indexLengthChange = false;


    // DataTables options for check_table (Check Constraints) table in
    // src/main/resources/layout/tables/table.html and src/main/resources/layout/constraint.html
    @Parameter(
        names = {
            "-noCheckPaging", "--noCheckPaging",
            "schemaspy.noCheckPaging"
        },
        descriptionKey = "noCheckPaging"
    )
    private boolean noCheckPaging = false;

    @Parameter(
        names = {
            "-checkPageLength", "--checkPageLength",
            "schemaspy.checkPageLength"
        },
        descriptionKey = "checkPageLength"
    )
    private int checkPageLength = 10;

    @Parameter(
        names = {
            "-checkLengthChange", "--checkLengthChange",
            "schemaspy.checkLengthChange"
        },
        descriptionKey = "checkLengthChange"
    )
    private boolean checkLengthChange = false;


    // DataTables options for routine_table (Routines) table in src/main/resources/layout/routines.html
    @Parameter(
        names = {
            "-noRoutinePaging", "--noRoutinePaging",
            "schemaspy.noRoutinePaging"
        },
        descriptionKey = "noRoutinePaging"
    )
    private boolean noRoutinePaging = false;

    @Parameter(
        names = {
            "-routinePageLength", "--routinePageLength",
            "schemaspy.routinePageLength"
        },
        descriptionKey = "routinePageLength"
    )
    private int routinePageLength = 50;

    @Parameter(
        names = {
            "-routineLengthChange", "--routineLengthChange",
            "schemaspy.routineLengthChange"
        },
        descriptionKey = "routineLengthChange"
    )
    private boolean routineLengthChange = false;


    // DataTables options for fk_table (Foreign Key Constraints) table in src/main/resources/layout/constraint.html
    @Parameter(
        names = {
            "-noFkPaging", "--noFkPaging",
            "schemaspy.noFkPaging"
        },
        descriptionKey = "noFkPaging"
    )
    private boolean noFkPaging = false;

    @Parameter(
        names = {
            "-fkPageLength", "--fkPageLength",
            "schemaspy.fkPageLength"
        },
        descriptionKey = "fkPageLength"
    )
    private int fkPageLength = 50;

    @Parameter(
        names = {
            "-fkLengthChange", "--fkLengthChange",
            "schemaspy.fkLengthChange"
        },
        descriptionKey = "fkLengthChange"
    )
    private boolean fkLengthChange = false;


    // DataTables options for column_table (Columns) table in src/main/resources/layout/column.html
    @Parameter(
        names = {
            "-noColumnPaging", "--noColumnPaging",
            "schemaspy.noColumnPaging"
        },
        descriptionKey = "noColumnPaging"
    )
    private boolean noColumnPaging = false;

    @Parameter(
        names = {
            "-columnPageLength", "--columnPageLength",
            "schemaspy.columnPageLength"
        },
        descriptionKey = "columnPageLength"
    )
    private int columnPageLength = 50;

    @Parameter(
        names = {
            "-columnLengthChange", "--columnLengthChange",
            "schemaspy.columnLengthChange"
        },
        descriptionKey = "columnLengthChange"
    )
    private boolean columnLengthChange = false;


    // DataTables options for all tables in src/main/resources/layout/anomalies.html
    @Parameter(
        names = {
            "-noAnomaliesPaging", "--noAnomaliesPaging",
            "schemaspy.noAnomaliesPaging"
        },
        descriptionKey = "noAnomaliesPaging"
    )
    private boolean noAnomaliesPaging = false;

    @Parameter(
        names = {
            "-anomaliesPageLength", "--anomaliesPageLength",
            "schemaspy.anomaliesPageLength"
        },
        descriptionKey = "anomaliesPageLength"
    )
    private int anomaliesPageLength = 10;

    @Parameter(
        names = {
            "-anomaliesLengthChange", "--anomaliesLengthChange",
            "schemaspy.anomaliesLengthChange"
        },
        descriptionKey = "anomaliesLengthChange"
    )
    private boolean anomaliesLengthChange = false;

    private List<String> unknownArgs;

    public boolean isHelpRequired() {
        return helpRequired;
    }

    public boolean isDbHelpRequired() {
        return dbHelpRequired;
    }

    public boolean isPrintLicense() {
        return printLicense;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isHtmlDisabled() {
        return nohtml;
    }

    public boolean isHtmlEnabled() {
        return !nohtml;
    }

    public boolean withImpliedRelationships() {
        return !noImplied;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getConnprops() {
        return connprops;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public String getSchema() {
        return schema;
    }

    public boolean isSingleSignOn() {
        return sso;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        if (Objects.nonNull(passwordFromPrompt)) {
            return passwordFromPrompt;
        }
        return password;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchemaMeta() {
        return schemaMeta;
    }

    public Integer getPort() {
        return port;
    }

    public GraphvizConfig getGraphVizConfig() {
        return graphvizConfig;
    }

    public HtmlConfig getHtmlConfig() {
        return htmlConfigCli;
    }

    public boolean useVizJS() {
        return useVizJS;
    }

    public int getDegreeOfSeparation() {
        return degreeOfSeparation;
    }

    public boolean isNoDbObjectPaging() {
        return noDbObjectPaging;
    }

    public int getDbObjectPageLength() {
        return dbObjectPageLength;
    }

    public boolean isDbObjectLengthChange() {
        return dbObjectLengthChange;
    }

    public boolean isNoTablePaging() {
        return noTablePaging;
    }

    public int getTablePageLength() {
        return tablePageLength;
    }

    public boolean isTableLengthChange() {
        return tableLengthChange;
    }

    public boolean isNoIndexPaging() {
        return noIndexPaging;
    }

    public int getIndexPageLength() {
        return indexPageLength;
    }

    public boolean isIndexLengthChange() {
        return indexLengthChange;
    }

    public boolean isNoCheckPaging() {
        return noCheckPaging;
    }

    public int getCheckPageLength() {
        return checkPageLength;
    }

    public boolean isCheckLengthChange() {
        return checkLengthChange;
    }

    public boolean isNoRoutinePaging() {
        return noRoutinePaging;
    }

    public int getRoutinePageLength() {
        return routinePageLength;
    }

    public boolean isRoutineLengthChange() {
        return routineLengthChange;
    }

    public boolean isNoFkPaging() {
        return noFkPaging;
    }

    public int getFkPageLength() {
        return fkPageLength;
    }

    public boolean isFkLengthChange() {
        return fkLengthChange;
    }

    public boolean isNoColumnPaging() {
        return noColumnPaging;
    }

    public int getColumnPageLength() {
        return columnPageLength;
    }

    public boolean isColumnLengthChange() {
        return columnLengthChange;
    }

    public boolean isNoAnomaliesPaging() {
        return noAnomaliesPaging;
    }

    public int getAnomaliesPageLength() {
        return anomaliesPageLength;
    }

    public boolean isAnomaliesLengthChange() {
        return anomaliesLengthChange;
    }

    void setUnknownArgs(List<String> unknownArgs) {
        this.unknownArgs = unknownArgs;
    }

    public List<String> unknownArgs() {
        return Collections.unmodifiableList(unknownArgs);
    }
}
