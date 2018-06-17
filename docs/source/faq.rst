.. |br| raw:: html

   <br />

FAQ
====

General
--------

Schema or Catalog name can't be null
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
This means that Schema or Catalog information could not be extracted from connection. |br|
I this case you need to add options ``-s [schemaName]`` or ``-cat [catalogName]`` |br|
In most cases for catalog you can use ``-cat %`` |br|
In mysql you can use same as ``-db`` |br|


OSX
----

Graphviz
~~~~~~~~~
There have been lots of issue with graphviz and OSX |br|
So install using brew ``brew install graphviz --with-librsvg --with-pango`` |br|
Depending on OSX version |br|
*Older than High Sierra*, add ``-renderer :quartz`` to the commandline |br|
*High Sierra or newer*, add ``-renderer :cairo`` to the commandline |br|
