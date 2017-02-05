Get Started
=====================================

Welcome in SchemaSpy we will do the best to simplify documentation process of your database

Running SchemaSpy
-----------------

You can easy run SchemaSpy from the command line:

.. code-block:: bash

    java -jar schemaspy.jar -t dbType -dp C:/sqljdbc4-3.0.jar -db dbName -host server -port 1433 [-s schema] -u user [-p password] -o outputDir

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
