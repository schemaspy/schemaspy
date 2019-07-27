.. |markdown_link| raw:: html

   <a href="https://daringfireball.net/projects/markdown/" target="_blank">markdown</a>

.. |br| raw:: html

   <br />

Advanced Usage
==============

.. _started_connection_props:

Supply Connection-properties
----------------------------

As an example running mysql with a new driver you'll get warning
    According to MySQL 5.5.45+, 5.6.26+ and 5.7.6+ requirements SSL connection must be established by default if explicit option isn't set. For compliance with existing applications not using SSL the verifyServerCertificate property is set to 'false'. You need either to explicitly disable SSL by setting useSSL=false, or set useSSL=true and provide truststore for server certificate verification.

This can be omited by addind connection property ``useSSL=false``

To add this connection property add following to commandline:
``-connprops useSSL\=false``

``-connprops`` can also take a properties file as argument but when escaping the ``=`` with ``\`` it will use it as "useSSL=false".
If key or value contains ``/`` it needs to be escaped with a single ``\``. Multiple pairs can be separated by ``;``.
If using linux shell or git bash in windows the ``\`` also needs to be escaped with a ``\`` so for example it would look like ``useSSL\\=false``.


Create your own DB type
-----------------------

As an example we will add the connection property from above to the mysql db-type

#. Create a new file in same directory as the schemaspy-jar, let's call it mysql-nossl.properties
#. Add the following content to mysql-nossl.properies::

    extends=mysql
    connectionSpec=jdbc:mysql://<hostOptionalPort>/<db>?useSSL=false

#. Now you can run schamaspy with -t mysql-nossl

If you want to have a closer look at the db-types you can find them at `github <https://github.com/schemaspy/schemaspy/tree/master/src/main/resources/org/schemaspy/types>`_

Supply or override database type
--------------------------------

#. Create a new file in same directory as the schemaspy-jar, let's call it myDbType.properties
#. Start by extending the database type you want to supply or override sql statements for as an example we will extends postgresSQL::

    extends=pgsql

#. Queries you can supply or override can be found at :ref:`databaseType-sql` we will override routines::

    extends=pgsql
    selectRoutinesSql=select r.routine_name, case p.prokind when 'f' then 'FUNCTION' when 'p' then 'PROCEDURE' when 'a' then 'AGGREGATE' when 'w' then 'WINDOW' else 'UNKNOWN' end as routine_type, case when p.proretset then 'SETOF ' else '' end || case when r.data_type = 'USER-DEFINED' then r.type_udt_name else r.data_type end as dtd_identifier, r.external_language as routine_body, r.routine_definition, r.sql_data_access, r.security_type, r.is_deterministic, d.description as routine_comment from information_schema.routines r left join pg_namespace ns on r.routine_schema = ns.nspname left join pg_proc p on ns.oid = p.pronamespace and r.routine_name = p.proname left join pg_description d on d.objoid = p.oid where r.routine_schema = :schema

#. We also want to add materialized views to view as document at :ref:`databaseType-other-properties`::

    extends=pgsql
    selectRoutinesSql=select r.routine_name, case p.prokind when 'f' then 'FUNCTION' when 'p' then 'PROCEDURE' when 'a' then 'AGGREGATE' when 'w' then 'WINDOW' else 'UNKNOWN' end as routine_type, case when p.proretset then 'SETOF ' else '' end || case when r.data_type = 'USER-DEFINED' then r.type_udt_name else r.data_type end as dtd_identifier, r.external_language as routine_body, r.routine_definition, r.sql_data_access, r.security_type, r.is_deterministic, d.description as routine_comment from information_schema.routines r left join pg_namespace ns on r.routine_schema = ns.nspname left join pg_proc p on ns.oid = p.pronamespace and r.routine_name = p.proname left join pg_description d on d.objoid = p.oid where r.routine_schema = :schema
    viewTypes=VIEW,MATERIALIZED VIEW

#. Now run schemaspy with your own database type ``-t myDbType``

Create you own DB type super advanced
-------------------------------------

Now we are going to connect to mysql thru unix socket, put on your helmets

#. Download a unix socket library for java and all of it's dependencies, for simplicity put them in a sub-folder called ``drivers`` in the same folder as the schemaspy-jar::

    junixsocket-common-2.0.4.jar
    junixsocket-mysql-2.0.4.jar
    junixsocket-native-2.0.4-x86_64-MacOSX-gpp-jni.nar <- Im on OSX
    junixsocket-native-2.0.4.nar
    mysql-connector-java-5.1.32.jar
    native-lib-loader-2.1.5.jar
    slf4j-api-1.7.25.jar
    slf4j-simple-1.7.25.jar

#. Create your own db-type let's call it my-mysql-socket.properties in same folder as the schemaspy-jar with following content::

    connectionSpec=jdbc:mysql://<host>/<db>?socketFactory=<socketFactory>&socket=<socket>
    socketFactory=ClassName of socket factory which must be in your classpath
    socket=Path To Socket

#. Now run schemaspy with the following options::

    java -jar [schemaspy.jar] -t my-mysq-socket \
    -dp lib/mysql-connector-java-[version].jar \
    -loadjars \
    -db [DBName] \
    -host localhost \
    -port 3306 \
    -u [User] \
    -socketFactory org.newsclub.net.mysql.AFUNIXDatabaseSocketFactory \
    -socket [pathToSocket] \
    -o [outputDir]

   Replace values accordingly. |br|
   Yes, you need to specify ``-db``, ``-host``, ``-port`` |br|
   Yes, the socketFactory could have been written directly into the properties-file, this is just an example, mysql-socket exists as a db-type exactly like this. |br|
   And since you might want to use another unix socket library this doesn't close any doors. |br|

.. _usage_advanced_markdown_metadata:

Add markdown comments using additional metadata
-----------------------------------------------

Schemaspy supports markdown in comments |markdown_link| |br|
Not all dbms supports comments or long enough comments or comments might just be missing. |br|

Schemaspy also supports supplying additional metadata :ref:`schemameta` |br|
More precise the ability to add/replace comments. :ref:`schemameta-comment`

.. code-block:: xml
   :linenos:
   :emphasize-lines: 4,5

   <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
     <comments>Database comment</comments>
        <tables>
           <table name="ACCOUNT" comments="I've added comment that links using markdown to markdown documentation [markdown](https://daringfireball.net/projects/markdown/)" >
               <column name="accountId" comments="And now the schemaspy avatar ![avatar](https://avatars3.githubusercontent.com/u/20635098?s=20&v=4 "SchemaSpy")" />
           </table>
        </tables>
   </schemaMeta>

Now just run with ``-meta [path-to-above-xml]``