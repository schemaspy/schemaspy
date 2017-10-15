Get Started
=====================================

Welcome to SchemaSpy. We will do the best to simplify documentation process of your database.

Configuration
-----------------

Parameters can be specified in the comand line (described below) or you can predefine configuration in the file.
SchemaSpy will search configuration file in 
``<current-dir>/schemaspy.properties``
To use an alternative configuration file run SchemaSpy with parameter: ``java -jar schemaspy.jar -configFile path/to/config.file``

Config file example: ::

	# type of database. Run with -dbhelp for details
	schemaspy.t=mssql
	# optional path to alternative jdbc drivers. 
	schemaspy.dp=path/to/drivers
	# database properties: host, port number, name user, password
	schemaspy.host=server
	schemaspy.port=1433
	schemaspy.db=db_name
	schemaspy.u=database_user
	schemaspy.p=database_password
	# output dir to save generated files
	schemaspy.o=path/to/output
	# db scheme for which generate diagrams
	schemaspy.s=dbo

Running SchemaSpy
-----------------

You can easily run SchemaSpy from the command line:

.. code-block:: bash

    java -jar schemaspy.jar -t dbType -dp C:/sqljdbc4-3.0.jar -db dbName -host server -port 1433 [-s schema] -u user [-p password] -o outputDir

Parameters priority:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

It is important to notice, that command-line parameters **override** those configured in schemaspy.properties file. 

Commonly used parameters:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

[-t databaseType]
    Type of database (e.g. ora, db2, etc.). Use -dbhelp for a list of built-in types. Defaults to ora.
[-db dbName]
    Name of database to connect to
[-u user]
    Valid database user id with read access. A user id is required unless -sso is specified.
[-s schema]
    Database schema. This is optional if it's the same as user or isn't supported by your database.
    Use -noschema if your database thinks it supports schemas but doesn't (e.g. older versions of Informix).
[-p password]
    Password associated with that user. Defaults to no password.
[-o outputDirectory]
    Directory to write the generated HTML/graphs to	
[-dp pathToDrivers]
    Looks for drivers here before looking in driverPath in [databaseType].properties.
    The drivers are usually contained in .jar or .zip files and are typically provided by your database vendor.
[-hq] or [-lq]
    Generate higher or lower-quality diagrams. Various installations of Graphviz (depending on OS and/or version) will default to generat /ing 
    either higher or lower quality images. That is, some might not have the "lower quality" libraries and others might not have the "higher quality" libraries.
    Higher quality output takes longer to generate and results in significantly larger image files (which take longer to download / display),
    but the resultant Entity Relationship diagrams generally look better.

Advanced Usage
---------------

Supply Connection-properties
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As an example running mysql with a new driver you'll get warning
    According to MySQL 5.5.45+, 5.6.26+ and 5.7.6+ requirements SSL connection must be established by default if explicit option isn't set. For compliance with existing applications not using SSL the verifyServerCertificate property is set to 'false'. You need either to explicitly disable SSL by setting useSSL=false, or set useSSL=true and provide truststore for server certificate verification.
This can be omited by addind connection property ``useSSL=false``

To add this connection property add following to commandline:
``-connprops useSSL\\=false``

``-connprops`` can also take a properties file as argument but when escaping the = with double \ it will use ut as "useSSL=false"

Create your own DB type
~~~~~~~~~~~~~~~~~~~~~~~

As an example we will add the connection property from above to the mysql db-type

#. Create a new file in same directory as the schemaspy-jar, let's call it mysql-nossl.properties
#. Add the following content to mysql-nossl.properies::

    extends=mysql
    connectionSpec=jdbc:mysql://<hostOptionalPort>/<db>?useSSL=false
#. Now you can run schamaspy with -t mysql-nossl

If you want to have a closer look at the db-types you can find them at `github <https://github.com/schemaspy/schemaspy/tree/master/src/main/resources/org/schemaspy/types>`_

Create you own DB type super advanced
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
| Replace values accordingly.
| Yes, you need to specify ``-db``, ``-host``, ``-port``
| Yes, the socketFactory could have been written directly into the properties-file, this is just an example, mysql-socket exists as a db-type exactly like this.
| And since you might want to use another unix socket library this doesn't close any doors.