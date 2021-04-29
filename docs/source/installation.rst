Installation
=====================================

Prerequisites
-------------

Before you can use SchemaSpy you must have the following prerequisites available on your local system.

* Java
* a JDBC driver file for your specific database engine 
* viz.js or Graphviz

Java
^^^^

You will need to have a supported version of Java installed, which includes:

* Java version 8 or higher
* OR OpenJDK 1.8 or higher - an open-source alternative

You can run ``java -version`` in a terminal to check the version of any currently installed Java.

If you don't already have a proper version of Java installed, see `OpenJDK <https://openjdk.java.net/install/>`_ or `Oracle Java <https://www.oracle.com/java/technologies/javase-downloads.html>`_ for download and install instructions for your operating systems.


JDBC Driver
^^^^^^^^^^^

No JDBC drivers are included with the jar-distribution of SchemaSpy.

In some case, a JDBC driver may already exist on your local system if your database happens to come with drivers. Otherwise, you will need to download one yourself.

If downloading a driver, you can usually find an approriate driver by searching the internet for "[name of your database] JDBC driver".

Verify the driver you download is compatible with the version of database management system / engine that you are using. For instance, if you're using PostgreSQL 13 the JDBC driver will need to support PostgreSQL 13.

Here is a list of where you might find drivers for common database management systems:

* `DB2 <https://www.ibm.com/support/pages/db2-jdbc-driver-versions-and-downloads>`_
* `Firebird <https://firebirdsql.org/en/jdbc-driver/>`_
* `Impala <https://impala.apache.org/docs/build/html/topics/impala_jdbc.html>`_
* `MySQL <https://www.mysql.com/products/connector/>`_
* `MariaDB <https://downloads.mariadb.org/connector-java/>`_
* `Netezza <https://www.ibm.com/support/knowledgecenter/SSULQD_7.2.1/com.ibm.nz.datacon.doc/c_datacon_installing_configuring_jdbc.html>`_
* `Oracle <https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html>`_
* `PostgreSQL <https://jdbc.postgresql.org/download.html>`_
* `Redshift <https://docs.aws.amazon.com/redshift/latest/mgmt/configuring-connections.html>`_
* `SQLite <https://github.com/xerial/sqlite-jdbc>`_
* `SQL Server <https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server>`_
* `Sybase <http://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.help.sqlanywhere.12.0.1/dbprogramming/jconnect-using-jdbxextra.html>`_
* `Teradata <https://downloads.teradata.com/download/connectivity/jdbc-driver>`_

You will need to tell SchemaSpy where to find the JDBC driver you require. For example, if you downloaded the Postgres JDBC file called postgresql-42.2.19.jar to the current directory the command would include the -dp argument, like ``-dp postgresql-42.2.19.jar``. 

If your JDBC .jar file is in a different directory, then the -dp argument would need to specify the directory path to the file, like ``-dp /opt/some-directory/your-jdbc-driver-name.jar``. 

See :ref:`commandline` for more information and advanced situations.

viz.js or Graphviz
^^^^^^^^^^^^^^^^^^
This is necessary to render graphical representations / images of the database relationships.

SchemaSpy version 6.1.0 and higher now comes with viz.js, so you will not need to download anything unless you're using SchemaSpy version 6.0 or lower.

For SchemaSpy version 6.1.0 or higher, simply include ``-vizjs`` as a command line argument when executing the SchemaSpy command.

NOTE: If you are using Java 15 or later, ``vis.js`` will not work! This is because Nashorn (JavaScript engine) was removed on Java 15. Hence, if you are on Java 15 or later, you need to install Graphviz manually as described below. Or, you can switch to an earlier Java version if you would like to use ``vis.js``.

If you must use Schemaspy version 6.0 or less, then Graphviz will need to be installed as follows.

- Windows
    The easiest way to install Graphviz is to download the msi package from `http://www.graphviz.org/download/ <http://www.graphviz.org/download/>`_
    
    .. warning::
        Remember to add the folder containing Graphviz's dot.exe application to your system PATH variable, eg.

        .. code-block:: bash

            C:\Program Files (x86)\Graphviz2.38\bin        

- Linux, Mac OS
    Please read carefully the detailed instructions on how to `install Graphviz on your operating system <http://www.graphviz.org/download/>`_.

Download SchemaSpy
------------------

SchemaSpy can be downloaded as a stand-alone, executable .jar file or Docker image.

Executable .jar File
^^^^^^^^^^^^^^^^^^^^

Download the latest stable executable .jar file (ex. schemaspy-[version].jar) from the `SchemaSpy website <http://schemaspy.org>`_ or `GitHub releases page <https://github.com/schemaspy/schemaspy/releases>`_.

For special cases, like testing a recent `bug fix or feature <https://github.com/schemaspy/schemaspy/issues>`_, we also make a `bleeding-edge build file <https://github.com/schemaspy/schemaspy#latest-build>`_ available. The bleeding-edge build may not be stable and should only be used for testing.

Proceed to the :ref:`get-started` section to learn how to configure and run the downloaded SchemaSpy executable.

Docker Image
^^^^^^^^^^^^

The latest `Docker <https://docs.docker.com/get-docker/>`_ image of SchemaSpy can be downloaded using `docker pull schemaspy/schemaspy`.

Docker documentation and run commands can be found on the `SchemaSpy Docker Hub page <https://hub.docker.com/r/schemaspy/schemaspy/>`_.
