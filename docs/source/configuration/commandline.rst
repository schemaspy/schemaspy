.. |br| raw:: html

   <br />

.. _commandline:

Command-Line Arguments
======================

Most of the command-line arguments can be specified in a properties file either with the default name schemaspy.properties
or in a file specified using ``-configFile`` the command-line arguments should be prefixed with schemaspy. As an example ``-sso`` would be ``schemaspy.sso`` and
``-u username`` would be ``schemaspy.u=username``.

General
-------
[-h]
    Print help message
[-dbhelp]
    Print databaseType required arguments
[-configFile filePath]
    Path to configFile to be used, default is to look for schemaspy.properties
[-o outputDirectory]
    Directory to write the generated HTML\/graphs to

Database related
----------------

Connecting
~~~~~~~~~~
[-t databaseType]
    Type of database (e.g. ora, db2, etc.). Use -dbhelp for a list of built-in types. Defaults to ora.
[-db dbName]
    Name of database to connect to.
[-host hostName]
    Hostname/ip to connect to, if required by databaseType.
[-port portNumber]
    Port that dbms listens to, if required by databaseType.
[-u user]
    Valid database user id with read access. A user id is required unless -sso is specified.
[-p password]
    Password associated with that user. Defaults to no password.
[-sso]
    Single sign-on, used when -u and -p should be ignored. See also :ref:`singlesignon`
[-pfp]
    Prompt for password, if you don't want to have password in command history.
[-connprops filePathOrKeyValue]
    Either a properties-file with additional properties or a key/value list, pairs separated by ``;``
    and key and value separated by ``\=`` example ``-connprops key1\=value1;key2\=value2`` see also :ref:`started_connection_props`.
    In linux shells or git bash on windows the ``\`` also needs to be escaped so it should look like ``\\=``. |br|
    ConnectionProperties will always be populated with -u and -p if they exist.
[-dp pathToDrivers]
    Where to search for jdbc drivers.
    The drivers are usually contained in .jar or .zip files and are typically provided by your database vendor.
    Multiple jars can be specified using os-specific path separator.

Processing
~~~~~~~~~~
[-cat catalog]
    Filter using a specific catalog this is usually the root of the database and contains schemas.
[-s schema]
    Database schema. This is optional if it's the same as user or isn't supported by your database.
[-schemas listOfSchemas]
    List of schemas to analyze, separated by ``,``
[-all]
    Try to analyze all schemas in database, schemas can be excluded with ``-schemaSpec`` which as defaults set by databaseType
[-schemaSpec schemaRegEx]
    Schemas to analyze, default to all, might be specified by databaseType.
[-dbthreads number]
    Specify how many threads/connections should be used when reading data from database, defaults to 15 or
    as specified by databaseType
[-norows]
    Skip fetching number of rows in tables.
[-noviews]
    Skip processing of views.
[-i includeTableRegex]
    Include table(s) in analysis, defaults to match everything
[-I excludeTableRegex]
    Exclude table(s) from analysis, defaults to exclude tables containing ``$``, can be overridden with ``-I ""``
[-\-include-routine-definition]
    Include routine definition in output

Additional data
~~~~~~~~~~~~~~~
[-meta pathToFolder]
    Augment the database by using :ref:`schemameta`. Specifying path to a folder containing files named [schema].meta.xml, if schema is not used it will look for [databaseName].meta.xml |br|
    One file per schema.

Html report related
-------------------
[-nohtml]
    Skip generation of html report.
[-noimplied]
    Don't look for implied relationships.
[-\-no-orphans]
    Don't include orphan tables in the relationships graph.
[-nopages]
    Just list data as one long list instead of pages.
[-rails]
    Use `Rails-based naming convention <https://gist.github.com/iangreenleaf/b206d09c587e8fc6399e#relations-in-models>`_ to find relationships between logical foreign keys and primary keys.
[-template path]
    Path to custom mustache template/css directory, needs to contain full set of templates.
    Bundled templates can be found in jar '/layout' and can be extracted with jar tool or any zip capable tool.
[-maxdet number]
    Threshold for details in diagrams, increase if you still want details, checked against sum of tables and views.
[-css fileName]
    Use a custom stylesheet. Bundled stylesheet can be extracted from jar(using zip capable tool), path '/layout/schemaSpy.css'
[-desc description]
    Add a description to the index page.
[-asciidoc]
    Uses asciidoc rather than markdown when processing descriptions

DataTables related
------------------

Parameters for *Tables* table in *src/main/resources/layout/main.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noDbObjectPaging]
    List data in table with no paging, i.e. all data in a single list.

[-dbObjectPageLength number]
    The number of items to list per page, if paging is enabled.

[-dbObjectLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Columns* table in *src/main/resources/layout/tables/table.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noTablePaging]
    List data in table with no paging, i.e. all data in a single list.

[-tablePageLength number]
    The number of items to list per page, if paging is enabled.

[-tableLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Indexes* table in *src/main/resources/layout/tables/{table_name}.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noIndexPaging]
    List data in table with no paging, i.e. all data in a single list.

[-indexPageLength number]
    The number of items to list per page, if paging is enabled.

[-indexLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Check Constraints* table in *src/main/resources/layout/tables/table.html* and *src/main/resources/layout/constraint.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noCheckPaging]
    List data in table with no paging, i.e. all data in a single list.

[-checkPageLength number]
    The number of items to list per page, if paging is enabled.

[-checkLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Routines* table in *src/main/resources/layout/routines.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noRoutinePaging]
    List data in table with no paging, i.e. all data in a single list.

[-routinePageLength number]
    The number of items to list per page, if paging is enabled.

[-routineLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Foreign Key Constraints* table in src/main/resources/layout/constraint.html
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noFkPaging]
    List data in table with no paging, i.e. all data in a single list.

[-fkPageLength number]
    The number of items to list per page, if paging is enabled.

[-fkLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *Columns* table in *src/main/resources/layout/column.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noColumnPaging]
    List data in table with no paging, i.e. all data in a single list.

[-columnPageLength number]
    The number of items to list per page, if paging is enabled.

[-columnLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.

Parameters for *all* tables in *src/main/resources/layout/anomalies.html*
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
[-noAnomaliesPaging]
    List data in table with no paging, i.e. all data in a single list.

[-anomaliesPageLength number]
    The number of items to list per page, if paging is enabled.

[-anomaliesLengthChange]
    Allow user of the webpage to change the number of items per page using a dropdown menu.


**Note: -nopages overrides all [-no*Paging] parameters.**


Diagram related
~~~~~~~~~~~~~~~
[-gv directoryPath]
    Path to directory containing graphviz executable(dot).
[-renderer :rendererName]
    Specify which renderer to use should be prefixed with ':' example ``-renderer :cairo``
[-hq] or [-lq]
    Generate higher or lower-quality diagrams. Various installations of Graphviz (depending on OS and/or version) will default to generating
    either higher or lower quality images. That is, some might not have the "lower quality" libraries and others might not have the "higher quality" libraries.
    Higher quality output takes longer to generate and results in significantly larger image files (which take longer to download/display),
    but the resultant Entity Relationship diagrams generally look better.
[-imageformat outputImageFormat]
    The format of the image that gets generated. Supported formats are svg and png. Defaults to png.
    E.g. ``-imageformat svg``
[-maxdet number]
    Limit for when tables shouldn't be detailed. Evaluated against total number of tables in schema. Defaults to 300.
[-font fontName]
    Change font used in diagrams, defaults to 'Helvetica'
[-fontsize number]
    Change font size in large diagrams, defaults to 11
[-rankdirbug]
    Switch diagram direction from 'top to bottom' to 'right to left'
[-X excludeColumnRegex]
    Exclude column(s), regular expression to exclude column(s) from diagrams, defaults to nothing.
[-x excludeIndirectColumnsRegex]
    Exclude column(s) from diagrams where column(s) aren't directly referenced by focal table, defaults to nothing.
[-vizjs]
    Use embedded viz.js instead of Graphviz. Useful when graphviz isn't installed. Memory is set to 64 MB, if you receive ther error "Cannot enlarge memory arrays" please report this to us.
[-degree 1 or 2]
    Limit the degree of separation (1 shows less, 2 is default), 1 is a good option for large databases with lots of relationships.
