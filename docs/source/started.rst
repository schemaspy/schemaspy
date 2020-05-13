Get Started
===========

Configuration
-------------

Parameters can be specified in the command line (described below) or you can predefine configuration in the file.
SchemaSpy will search configuration file in 
``<current-dir>/schemaspy.properties``
To use an alternative configuration file run SchemaSpy with parameter: ``java -jar schemaspy.jar -configFile path/to/config.file``

Config file example: ::

	# type of database. Run with -dbhelp for details
	# if mssql doesn't work: try mssql08 in combination with sqljdbc_7.2, this combination has been tested
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

Parameters priority
~~~~~~~~~~~~~~~~~~~~

It is important to notice, that command-line parameters **override** those configured in schemaspy.properties file. 

Commonly used parameters
~~~~~~~~~~~~~~~~~~~~~~~~~

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
    Supports a directory as argument, which will add directory and all content to classpath, will recurse.
    Supports multiple paths separated by OS dependent path separator
[-hq] or [-lq]
    Generate higher or lower-quality diagrams. Various installations of Graphviz (depending on OS and/or version) will default to generat /ing 
    either higher or lower quality images. That is, some might not have the "lower quality" libraries and others might not have the "higher quality" libraries.
    Higher quality output takes longer to generate and results in significantly larger image files (which take longer to download / display),
    but the resultant Entity Relationship diagrams generally look better.
[-imageformat outputImageFormat]
    The format of the image that gets generated. Supported formats are svg and png. Defaults to png. 
    E.g. ``-imageformat svg``

For a comprehensive listing see :ref:`commandline`
