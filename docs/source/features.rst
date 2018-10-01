.. |er_wiki_link| raw:: html

   <a href="https://en.wikipedia.org/wiki/Entity%E2%80%93relationship_model" target="_blank">ER diagram</a>

.. |markdown_link| raw:: html

   <a href="https://daringfireball.net/projects/markdown/" target="_blank">markdown</a>

Features
--------

* Supports most JDBC compliant dbms (support missing? you can add your own)
* Generates |er_wiki_link| for foreign keys
* Generates |er_wiki_link| for implied relationships (name, type) of a column matches a primary key
* Generates |er_wiki_link| for relationships based on rails naming conventions
* Shows column relationship and actions
* Shows routines (Functions/Stored procedures)
* Shows views and definitions
* Will render |markdown_link| present in comments
* Allows for supplying additional metadata, see :ref:`schemameta`
* Present a set of found anomalies