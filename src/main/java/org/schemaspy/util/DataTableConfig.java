package org.schemaspy.util;

import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.view.HtmlConfig;

import java.util.HashMap;
import java.util.Map;

public class DataTableConfig {

    // DataTables options for database_objects (Tables) table in src/main/resources/layout/main.html
    private final Parameters databaseObjects;

    // DataTables options for standard_table (Columns) table in src/main/resources/layout/tables/table.html
    private final Parameters standardTable;

    // DataTables options for indexes_table (Indexes) table in src/main/resources/layout/tables/table.html
    private final Parameters indexesTable;

    // DataTables options for check_table (Check Constraints) table in
    // src/main/resources/layout/tables/table.html and src/main/resources/layout/constraint.html
    private final Parameters checkTable;

    // DataTables options for routine_table (Routines) table in src/main/resources/layout/routines.html
    private Parameters routineTable;

    // DataTables options for fk_table (Constraints) table in src/main/resources/layout/constraint.html
    private Parameters fkTable;

    // DataTables options for column_table (Columns) table in src/main/resources/layout/column.html
    private Parameters columnTable;

    // DataTables options for all tables in src/main/resources/layout/anomalies.html
    private Parameters anomalies;

    private Map<String, Object> pageScopeMap = new HashMap<>();


    private static class Parameters {
        boolean noPaging;
        int pageLength;
        boolean lengthChange;

        public Parameters(boolean noPaging, int pageLength, boolean lengthChange) {
            this.noPaging = noPaging;
            this.pageLength = pageLength;
            this.lengthChange = lengthChange;
        }
    }


    public DataTableConfig(CommandLineArguments commandLineArguments) {

        this.databaseObjects = new Parameters(
                commandLineArguments.isNoDbObjectPaging(),
                commandLineArguments.getDbObjectPageLength(),
                commandLineArguments.isDbObjectLengthChange());
        this.pageScopeMap.put(
            "databaseObjects",
            getParameterMap(
                this.databaseObjects,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.standardTable = new Parameters(
                commandLineArguments.isNoTablePaging(),
                commandLineArguments.getTablePageLength(),
                commandLineArguments.isTableLengthChange());
        this.pageScopeMap.put(
            "standardTable",
            getParameterMap(
                this.standardTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.indexesTable = new Parameters(
                commandLineArguments.isNoIndexPaging(),
                commandLineArguments.getIndexPageLength(),
                commandLineArguments.isIndexLengthChange());
        this.pageScopeMap.put(
            "indexesTable",
            getParameterMap(
                this.indexesTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.checkTable = new Parameters(
                commandLineArguments.isNoCheckPaging(),
                commandLineArguments.getCheckPageLength(),
                commandLineArguments.isCheckLengthChange());
        this.pageScopeMap.put(
            "checkTable",
            getParameterMap(
                this.checkTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.routineTable = new Parameters(
                commandLineArguments.isNoRoutinePaging(),
                commandLineArguments.getRoutinePageLength(),
                commandLineArguments.isRoutineLengthChange());
        this.pageScopeMap.put(
            "routineTable",
            getParameterMap(
                this.routineTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.fkTable = new Parameters(
                commandLineArguments.isNoFkPaging(),
                commandLineArguments.getFkPageLength(),
                commandLineArguments.isFkLengthChange());
        this.pageScopeMap.put(
            "fkTable",
            getParameterMap(
                this.fkTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.columnTable = new Parameters(
                commandLineArguments.isNoColumnPaging(),
                commandLineArguments.getColumnPageLength(),
                commandLineArguments.isColumnLengthChange());
        this.pageScopeMap.put(
            "columnTable",
            getParameterMap(
                this.columnTable,
                commandLineArguments.getHtmlConfig()
            )
        );

        this.anomalies = new Parameters(
                commandLineArguments.isNoAnomaliesPaging(),
                commandLineArguments.getAnomaliesPageLength(),
                commandLineArguments.isAnomaliesLengthChange());
        this.pageScopeMap.put(
            "anomalies",
            getParameterMap(
                this.anomalies,
                commandLineArguments.getHtmlConfig()
            )
        );
    }


    public Map<String, Object> getParameterMap(Parameters parameters, HtmlConfig htmlConfig) {
        boolean isPaginationEnabled = htmlConfig.isPaginationEnabled();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("paging", (isPaginationEnabled && (!parameters.noPaging)));
        parameterMap.put("pageLength", parameters.pageLength);
        parameterMap.put("lengthChange", parameters.lengthChange);
        return parameterMap;
    }


    public Map<String, Object> getPageScopeMap() {
        return this.pageScopeMap;
    }

}
