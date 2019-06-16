.. |br| raw:: html

   <br />

.. _singlesignon:

Single Sign-On
==============

mssql
~~~~~
When running SchemaSpy |br|
**before** ``-jar SchemaSpy-[version].jar`` |br|
add ``-Djava.library.path=[path-to-dir-containing-sqljdbc_auth.dll]`` |br|
**after** ``-jar SchemaSpy-[version].jar`` |br|
add ``-sso`` |br|
When using cmd add ``-connprops integratedSecurity\=true`` |br|
When using git bash in windows add ``-connprops integratedSecurity\\=true`` |br|

mssql-jtds
~~~~~~~~~~
When running SchemaSpy |br|
**before** ``-jar SchemaSpy-[version].jar`` |br|
add ``-Djava.library.path=[path-to-dir-containing-ntlmauth.dll]`` |br|
**after** ``-jar SchemaSpy-[version].jar`` |br|
add ``-sso`` |br|