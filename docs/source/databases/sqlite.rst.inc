.. |xerial| raw:: html

   <a href="https://github.com/xerial/sqlite-jdbc" target="_blank">Xerial sqlite-jdbc</a>

SQLite
====================

.. _instructions:

Instructions
--------------

Recommended driver is the xerial driver: |xerial| |br|
Driver can be downloaded from releases |br|
To use the driver specify ``-t sqlite-xerial``

If connecting without user/pass the ``-sso`` argument can be used.

Arguments: |br|
``-s`` same as database name
``-cat`` same as database name
``-db`` full path to database file

Example layout on windows: |br|

To avoid possible issues with space and strange characters. |br|
Base directory ``C:\Tmp\sqlite`` |br|
place ``schemaspy-[version].jar`` in the root of that folder. |br|
place ``sqlite-jdbc-[version].jar`` in a subdir ``driver`` |br|
place ``[databasefile].db`` in a subdir ``db`` |br|

If we use chinook.db as an example we will have the following layout::

    C:\Tmp\sqlite\schemaspy-6.1.1-SNAPSHOT.jar
    C:\Tmp\sqlite\db\chinook.db
    C:\Tmp\sqlite\driver\sqlite-jdbc-3.36.0.3.jar

Place the command prompt in ``C:\Tmp\sqlite`` |br|
Now you can run::

    java -jar schemaspy-6.1.1-SNAPSHOT.jar -t sqlite-xerial -sso -o out -dp driver -db C:/Tmp/sqlite/db/chinook.db -s chinook -cat chinook

Output will be written to ``C:\Tmp\sqlite\out`` |br|

Notice how the path is written in the ``-db`` argument |br|
instead of ``C:\Tmp\sqlite\db\chinook.db`` |br|
it's written ``C:/Tmp/sqlite/db/chinook.db``
