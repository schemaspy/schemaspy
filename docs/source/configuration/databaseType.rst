.. |br| raw:: html

   <br />

.. _databaseType:

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

.. _databaseType-layout:

Layout
------

It can contain wast amounts of properties so we will break it down.
The Properties-file can contain instructions.

extends
    ``extends`` which does what it means, it allows one to override or add
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

dbms=
    Database Management System should general without version, used for grouping
description=
    Description for this specific databaseType (mostly used in logging) without dbms
connectionSpec=
    We will talk more about this one. It's the connectionUrl used, but it supports token replacement
driver=
    FQDN of the JDBC driver as an example ``org.h2.Driver``

.. _databaseType-connectionSpec:

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

.. _databaseType-other-properties:

Other Properties
----------------

dbThreads=
    number of threads that can be used to analyze the database
schemaSpec=
    regular expression used in conjunction with ``-all`` (and can be command line param ``-schemaSpec``)
tableTypes=
    Which types should be considered tables, default is TABLE
viewTypes=
    Which types should be considered views, default is VIEW
multirowdata=
    If rows with same keys/ids should have it's data appended to the first result, default is false

.. _databaseType-sql:

Sql query instead of DatabaseMetaData
-------------------------------------

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

**Possible overrides:**
    selectSchemasSql=
        *Fetch comments for a schema, expected columns:* |br| **schema_comment**
    selectCatalogsSql=
        *Fetch comments for a catalog, expected columns:* |br| **catalog_comment**
    selectTablesSql=
        *Fetch tables, expected columns:* |br| **table_name, table_catalog, table_schema, table_comment, table_rows**
    selectViewsSql=
        *Fetch views, expected columns:* |br| **view_name, view_catalog, view_schema, view_comment, view_definition**
    selectIndexesSql=
        *Fetch indexes, expected columns:* |br| **INDEX_NAME, TYPE, NON_UNIQUE, COLUMN_NAME, ASC_OR_DESC**
    selectPrimaryKeysSql=
        *Fetch table PKs, expected columns:* |br| **TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, KEY_SEQ, PK_NAME**
    selectRowCountSql=
        *Fetch row count for a table, expected columns:* |br| **row_count**
    selectColumnTypesSql=
        *Fetch column type for all columns, expected columns:* |br| **table_name, column_name, column_type, short_column_type**
    selectRoutinesSql=
        *Fetch routines, expected columns:* |br| **routine_name, routine_type, dtd_identifier, routine_body, routine_definition,sql_data_access, security_type, is_deterministic, routine_comment**
    selectRoutineParametersSql=
        *Fetch parameters for routines, expected columns:* |br| **specific_name, parameter_name, dtd_identifier, parameter_mode**
    selectViewSql=
        *Fetch definition for a view, expected columns:* |br| **view_definition, text (text has been deprecated)**
    selectViewCommentsSql=
        *Fetch comments for all views, expected columns:* |br| **view_name, comments**
    selectViewColumnCommentsSql=
        *Fetch column comments for all views, expected columns:* |br| **view_name|table_name, column_name, comments**
    selectCheckConstraintsSql=
        *Fetch check constraints for all tables, expected columns:* |br| **table_name, constraint_name, text**
    selectTableIdsSql=
        *Fetch ids for all tables, expected columns:* |br| **table_name, table_id**
    selectIndexIdsSql=
        *Fetch ids for all indexes, expected columns:* |br| **table_name, index_name, index_id**
    selectTableCommentsSql=
        *Fetch comments for all tables, expected columns:* |br| **table_name, comments**
    selectColumnCommentsSql=
        *Fetch comments for all columns, expected columns:* |br| **table_name, column_name, comments**
    selectSequencesSql=
        *Fetch all sequences from the database, expected columns:* |br| **sequence_name, start_value, increment** |br| **start_value** and **increment** defaults to 1 if missing


Included
--------

.. dbtypes::

