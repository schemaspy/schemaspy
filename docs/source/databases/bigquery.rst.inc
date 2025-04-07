Google BigQuery
====================

Support for Google BigQuery requires downloading the Simba JDBC driver. The
latest version is found at `Current JDBC Drivers`_

Unzip the ZIP archive in a convenient location

Extract the bigquery properties file from the schemaspy JAR:

.. code-block:: shell

  $ cd /tmp
  $ jar xvf ~/libs/jars/schemaspy-6.2.4.jar BOOT-INF/classes/org/schemaspy/types/bigquery.properties
  $ mv BOOT-INF/classes/org/schemaspy/types/bigquery.properties .


You need to choose your authentication mechanism. The properties file
contains each of the possible options on commented out lines. Choose
the mechanism which matches your needs and leave the others commented-out.

Replace **ALL** the parameters which start with `@@` in your chosen
mechanism. The documentation for the driver is found with each release
of the driver, with extensive information in "Configuring Authentication".

When you invoke SchemaSpy, you need to use these command line arguments:

.. code-block:: shell

    -t /path/to/bigquery.properties \
    -dp /path/to/unzipped-simba-archive \
    -loadjars -sso

In addition, if your Project contains many datasets, you should consider
listing only the datasets that you wish to analyze with the `-schemas`
argument.

.. _Current JDBC Drivers: https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers#current_jdbc_driver
