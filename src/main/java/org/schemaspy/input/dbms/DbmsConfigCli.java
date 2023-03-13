package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.PatternConverter;
import org.schemaspy.input.dbms.config.PropertiesResolver;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

@Parameters(resourceBundle = "dbmsconfigcli")
public class DbmsConfigCli implements DbmsConfig {

    @Parameter(
        names = {
            "-noexportedkeys", "--no-exported-keys",
            "schemaspy.noexportedkeys", "schemaspy.no-exported-keys"
        },
        descriptionKey = "noexportedkeys"
    )
    private boolean noExportedKeys = false;

    @Parameter(
        names = {
            "-noviews", "--no-views",
            "schemaspy.noviews", "schemaspy.no-views"
        },
        descriptionKey = "noviews"
    )
    private boolean noViews = false;

    @Parameter(
        names = {
            "-X", "--column-exclusions",
            "schemaspy.X", "schemaspy.columnExclusions", "schemaspy.column-exclusions"
        },
        descriptionKey = "columnexclusion",
        converter = PatternConverter.class
    )
    private Pattern columnExclusions = Pattern.compile("[^.]");

    @Parameter(
        names = {
            "-x", "--indirect-column-exclusions",
            "schemaspy.x", "schemaspy.indirectColumnExclusions", "schemaspy.indirect-column-exclusions"
        },
        descriptionKey = "indirectcolumnexclusions",
        converter = PatternConverter.class
    )
    private Pattern indirectColumnExclusions = Pattern.compile("[^.]");

    @Parameter(
        names = {
            "-i", "--table-inclusions",
            "schemaspy.i", "schemaspy.tableInclusions", "schemaspy.table-inclusions"
        },
        descriptionKey = "tableinclusions",
        converter = PatternConverter.class
    )
    private Pattern tableInclusions = Pattern.compile(".*");

    @Parameter(
        names = {
            "-I", "--table-exclusions",
            "schemaspy.I", "schemaspy.tableExclusions", "schemaspy.table-exclusions"
        },
        descriptionKey = "tableexclusions",
        converter = PatternConverter.class
    )
    private Pattern tableExclusions = Pattern.compile(".*\\$.*");

    @Parameter(
        names = {
            "-all",
            "schemaspy.all"
        },
        descriptionKey = "all"
    )
    private boolean evaluateAllEnabled = false;

    @Parameter(
        names = {
            "-schemaSpec", "--schema-spec",
            "schemaspy.schemaSpec", "schemaspy.schema-spec"
        },
        descriptionKey = "schemaspec"
    )
    private String schemaSpec;

    @Parameter(
        names = {
            "-t", "--database-type",
            "schemaspy.t", "schemaspy.database-type"
        },
        descriptionKey = "database-type"
    )
    private String databaseType = "ora";

    private final PropertiesResolver propertiesResolver;
    private Properties databaseTypeProperties = null;

    private NoRowsConfigCli noRowsConfigCli;

    public DbmsConfigCli(
        NoRowsConfigCli noRowsConfigCli,
        PropertiesResolver propertiesResolver
    ) {
        this.noRowsConfigCli = noRowsConfigCli;
        this.propertiesResolver = propertiesResolver;
    }

    public boolean isExportedKeysEnabled() {
        return !noExportedKeys;
    }

    @Override
    public boolean isNumRowsEnabled() {
        return noRowsConfigCli.isNumRowsEnabled();
    }

    @Override
    public boolean isViewsEnabled() {
        return !noViews;
    }

    @Override
    public Pattern getColumnExclusions() {
        return columnExclusions;
    }

    @Override
    public Pattern getIndirectColumnExclusions() {
        return indirectColumnExclusions;
    }

    @Override
    public Pattern getTableInclusions() {
        return tableInclusions;
    }

    @Override
    public Pattern getTableExclusions() {
        return tableExclusions;
    }

    @Override
    public boolean isEvaluateAllEnabled() {
        return evaluateAllEnabled;
    }

    @Override
    public String getSchemaSpec() {
        if (Objects.nonNull(schemaSpec)) {
            return schemaSpec;
        } else if (Objects.nonNull(getDatabaseTypeProperties().getProperty("schemaSpec"))) {
            return getDatabaseTypeProperties().getProperty("schemaSpec");
        } else {
            return ".*";
        }
    }

    @Override
    public String getDatabaseType() {
        return databaseType;
    }

    public synchronized Properties getDatabaseTypeProperties() {
        if (Objects.isNull(databaseTypeProperties)) {
            databaseTypeProperties = propertiesResolver.getDbProperties(getDatabaseType());
        }
        return databaseTypeProperties;
    }
}
