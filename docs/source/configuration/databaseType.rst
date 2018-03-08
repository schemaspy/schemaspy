DatabaseType
============

You can create you're own databaseType so lets go through how it works.

Selection
---------

On the commandline you specify the databaseType using the option ``-t``.
The option can be specified with either [name].properties or just [name]
the .properties will be added if missing. So if you create one, be sure
to have .properties extension.

Example:
 ``-t mysql``
or
 ``-t mysql.properties``

The search order is:
    1. user.dir/
    2. Classpath
    3. Classpath in schemaspy supplied location

This actually means that if you supply ``-t my_conf/mydbtype``

It will look for:
    1. file: $user.dir/my_conf/mydbtype.properties
    2. Classpath: my_conf/mydbtype.properties
    3. Classpath: org/schemaspy/types/my_conf/mydbtype.properties

Layout
------

It can contain wast amount of properties so we will break it down.
The Properties-file can contain instructions.

extends
    ``extends`` which does what i means, it allows one to override or add
    properties to an existing databaseType (by specifying a parent/base)

    **As an example**::

        extends=mysql

    which you can see in mysql-socket.properties

include
    ``include.[n]`` is a bit different it allows one to add a single property from another
    databaseType. ``[n]`` is substituted for a number. The value has the form of ``[databaseType]::[key]``.

    **As an example**::

        include.1=mysql::schemaSpec

    This would have been valid in the mariadb.properties

**Then we have required properties**:

description=
    Description for the databaseType (mostly used in logging)
connectionSpec=
    We will talk more about this one. It's the connectionUrl used, but it supports token replacement
driver=
    FQDN of the JDBC driver as an example ``org.h2.Driver``

ConnectionSpec
--------------

Let's dive a bit deeper into the connectionSpec.

**As an example from mysql-socket**::

    extends=mysql
    connectionSpec=jdbc:mysql://<host>/<db>?socketFactory=<socketFactory>&socket=<socket>
    socketFactory=ClassName of socket factory which must be in your classpath
    socket=Path To Socket

We mentioned extends earlier. |br|
ConnectionSpec contains the connectionUrl used with the jdbc driver, some might refer to it as the connectionString.

connectionSpec allow token replacement, a token is ``<[tokenName]>``. |br|
In the above example we have host, db, socketFactory, socket.

This means that when used it expects the following commandline arguments::

    -h [host] (for host)
    -db [dbname] (for db)
    -socketFactory [socketFactory class]
    -socket [path to socket]

host and db are already known, but ``-socketFactory`` and ``-socket`` has become a new commandline argument.
The presence of the keys in the databaseType properties file is only for description, it's printed when ``-dbhelp`` is used as a commandline argument.
(db and host located in databaseType mysql which is extended)

There is also a synthetic token that can be replaced <hostOptionalPort> which combines host and port if port is supplied. |br|
Default separator is ``:`` but can be changed by specifying another under the key ``hostPortSeparator``

Other Properties
----------------

driverPath=
    path to classpath resources that will be used when trying to create the jdbc Driver in java
    same as commandline argument ``-dp``
dbThreads=
    number of threads that can be used to analyze the database
schemaSpec=
    regular expression used in conjunction with ``-all`` (and can be command line param ``-schemaSpec``)

When metadata in JDBC isn't cutting the mustard. You can replace it with a sql query.
They are prepared and supports named parameters as long as they are available. Data is retrieved by column label.
So additional columns are ok, but you might need to alias columns so that they are returned correctly to schemaspy.

    :dbname
        DatabaseName ``-db``
    :schema
        Schema ``-s``
    :owner
        alias for :schema
    :table
        table that the query relates to (think selectRowCountSql)
    :view
        alias for :table
    :catalog
        Catalog ``-cat``

**Possible Metadata overrides and expected columns in result**:
    **selectSchemasSql=**
        schema_comment
    **selectCatalogsSql=**
        catalog_comment
    **selectTablesSql=**
        table_name, table_catalog, table_schema, table_comment, table_rows
    **selectViewsSql=**
        view_name, view_catalog, view_schema, view_comment, view_definition
    **selectIndexesSql=**
        INDEX_NAME, TYPE, NON_UNIQUE, COLUMN_NAME, ASC_OR_DESC
    **selectRowCountSql=**
        row_count
    **selectColumnTypesSql=**
        table_name, column_name, column_type, short_column_type
    **selectRoutinesSql=**
        routine_name, routine_type, dtd_identifier, routine_body, routine_definition,sql_data_access, security_type, is_deterministic, routine_comment
    **selectRoutineParametersSql=**
        specific_name, parameter_name, dtd_identifier, parameter_mode
    **selectViewSql=**
        view_definition, text (text has been deprecated)
    **selectCheckConstraintsSql=**
        table_name, constraint_name
    **selectTableIdsSql=**
        table_name, table_id
    **selectIndexIdsSql=**
        table_name, index_name, index_id
    **selectTableCommentsSql=**
        table_name, comments
    **selectColumnCommentsSql=**
        table_name, column_name, comments

**Define viewTypes**
    **viewTypes=**
        default is VIEW