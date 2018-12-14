What's new
===========

6.0.0
-----

* Html report
    * Now uses mustache
    * DataTables for data
    * Markdown rendering of comments

* DatabaseTypes:
    * sqlite-xerial
    * redshift
    * orathin-service
    * netezza
    * mysql-socket
    * mssql08
    * mssql08-jtds
    * mssql08-jtds-instance
    * impala
    * hive
    * hive-kerberos-driverwrapper
    * hive-kerberos-driverwrapper-zookeeper

6.1.0
-----

* Diagrams
    * Now has option to use embedded viz.js (no need for Graphviz) ``-vizjs``
    * Choose the relation depth degree 1 or 2 to simplify relation graphs for database with lot of tables and relations

* XML
    * Now includes routines

* Html report
    * Column page loads faster
    * Table page contains check constraints