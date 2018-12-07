.. |br| raw:: html

   <br />

.. _singlesignon:

Single Sign-On
==============

mssql
~~~~~
When running SchemaSpy |br|
**before** ``-jar SchemaSpy-[version].jar`` |br|
add ``-Djava.library.path=[path-to-sqljdbc_auth.dll]`` |br|
**after** ``-jar SchemaSpy-[version].jar`` |br|
add ``-sso`` |br|
add ``-connprops integratedSecurity\\=true`` |br|

mssql-jtds
~~~~~~~~~~
When running SchemaSpy |br|
**before** ``-jar SchemaSpy-[version].jar`` |br|
add ``-Djava.library.path=[path-to-ntlmauth.dll]`` |br|
**after** ``-jar SchemaSpy-[version].jar`` |br|
add ``-sso`` |br|