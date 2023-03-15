package org.schemaspy.input.dbms;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.schemaspy.cli.NoRowsConfigCli;
import org.schemaspy.cli.PatternConverter;
import org.schemaspy.cli.SchemasListConverter;
import org.schemaspy.input.dbms.config.PropertiesResolver;
import org.schemaspy.input.dbms.exceptions.RuntimeIOException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Parameter(
        names = {
            "-schemas", "-schemata",
            "schemaspy.schemas", "schemaspy.schemata"
        },
        descriptionKey = "schemas",
        listConverter = SchemasListConverter.class
    )
    private List<String> schemas = Collections.emptyList();

    @Parameter(
        names = {
            "-dbThreads", "-dbthreads", "--db-threads",
            "schemaspy.dbthreads", "schemaspy.dbThreads", "schemaspy.db-threads"
        },
        descriptionKey = "maxdbthreads"
    )
    private int maxDbThreads = 0;

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

    @Override
    public List<String> getSchemas() {
        return schemas;
    }

    @Override
    public int getMaxDbThreads() {
        if (maxDbThreads != 0) {
            return maxDbThreads;
        } else if (getDatabaseTypeProperties().containsKey("dbThreads")) {
            return Integer.parseInt(getDatabaseTypeProperties().getProperty("dbThreads"));
        } else if (getDatabaseTypeProperties().containsKey("dbthreads")) {
            return Integer.parseInt(getDatabaseTypeProperties().getProperty("dbthreads"));
        } else {
            return Math.min(Runtime.getRuntime().availableProcessors(), 15);
        }
    }

    @Override
    public Set<String> getBuiltInDatabaseTypes() {
        Enumeration<URL> typesFolders = urlsForTypesFolders();
        Set<String> dbTypes = new HashSet<>();
        while (typesFolders.hasMoreElements()) {
            URL typeFolder = typesFolders.nextElement();
            Path typeFolderPath = asPath(typeFolder);
            dbTypes.addAll(collectDbTypes(typeFolderPath));
        }
        return dbTypes;
    }

    private Enumeration<URL> urlsForTypesFolders() {
        try {
            return getClass().getClassLoader().getResources("org/schemaspy/types");
        } catch (IOException e) {
            throw new RuntimeIOException("Unable to retrieve urls for type folders", e);
        }
    }

    private Path asPath(URL typeFolder) {
        try {
            if (typeFolder.getProtocol().equalsIgnoreCase("file")) {
                return Paths.get(typeFolder.toURI());
            }
            ensureFileSystemExists(typeFolder);
            URI uri = URI.create(typeFolder.toString().replace("classes!", "classes"));
            return Paths.get(uri);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeIOException("Unable to create Path for '" + typeFolder + "'", e);
        }
    }

    private void ensureFileSystemExists(URL url) throws URISyntaxException, IOException {
        try {
            FileSystems.getFileSystem(url.toURI());
        } catch (FileSystemNotFoundException notFound) {
            FileSystems.newFileSystem(url.toURI(), Collections.singletonMap("create", "false"));
        }
    }

    private Set<String> collectDbTypes(Path typeFolderPath) {
        try (Stream<Path> pathStream = Files.list(typeFolderPath)) {
            return pathStream
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.matches(".*\\.properties$"))
                .map(name -> name.replaceAll("\\.properties$", ""))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeIOException("Unable to retrieve dbtypes from '" + typeFolderPath + "'", e);
        }
    }
}
