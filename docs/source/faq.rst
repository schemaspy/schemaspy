.. |br| raw:: html

   <br />

Frequent Asked Questions
=========================

General
--------

Schema or Catalog name can't be null
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
This means that Schema or Catalog information could not be extracted from connection. |br|
I this case you need to add options ``-s [schemaName]`` or ``-cat [catalogName]`` |br|
In most cases for catalog you can use ``-cat %`` |br|
In mysql you can use same as ``-db`` |br|

"Cannot enlarge memory arrays" when using viz.js
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
According to viz.js documentation the memory is default 16MB this should be enough. |br|
We have increased this to 64 MB if you receive this error, please report this to us. |br|

I just receive a cryptic error like "ERROR - null"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The code has previously avoided to log stracktraces, we now log them but only when |br|
``-debug`` is used. So any cryptic error can be enhanced with stacktrace by running |br|
SchemaSpy with the argument ``-debug`` |br|

OSX
----

Graphviz
~~~~~~~~~
There have been lots of issue with graphviz and OSX. |br|

Installing graphviz should include librsvg and pango by default when running ``brew install graphviz``.|br|

For older versions of OSX/brew you may need to run ``brew install graphviz --with-librsvg --with-pango`` |br|
Depending on OSX version |br|
*Older than High Sierra*, add ``-renderer :quartz`` to the commandline |br|
*High Sierra or newer*, add ``-renderer :cairo`` to the commandline |br|

Markdown
--------

Links to other objects in the documentation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
`[xyz]` will be parsed as link to the table/view named `xyz` in the current schema
