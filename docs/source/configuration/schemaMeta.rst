SchemaMeta
==========

Is a way to modify input that will affect output from SchemaSpy.

    * :ref:`schemameta-comment`
    * :ref:`schemameta-add-foreignKey`
    * :ref:`schemameta-add-remote-tables`
    * :ref:`schemameta-add-columns`
    * :ref:`schemameta-disableImpliedKeys`
    * :ref:`schemameta-disableDiagramAssociations`

All these instructions are defined in xml the schema can be found |xsd_link|

Schema contains documentation but lets go through the above mentioned features.

.. _schemameta-comment:

Add comments/remarks
--------------------

The xsd currently allows both comments and remarks. However remarks has been deprecated.

So adding a comment will either add, if missing from database, or replace if comments/remarks exist.

.. code-block:: xml
    :linenos:
    :emphasize-lines: 2,4,5

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <comments>Database comment</comments>
        <tables>
            <table name="ACCOUNT" comments="Table comment">
                <column name="accountId" comments="Column comment"/>
            </table>
        </tables>
    </schemaMeta>

.. _schemameta-add-foreignKey:

Add relationships
-----------------

.. code-block:: xml
    :linenos:
    :emphasize-lines: 5,8

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <tables>
            <table name="AGENT">
                <column name="acId" type="INT">
                    <foreignKey table="ACCOUNT" column="accountId" />
                </column>
                <column name="coId" type="INT">
                    <foreignKey table="COMPANY" column="companyId" />
                </column>
            </table>
        </tables>
    </schemaMeta>

.. _schemameta-add-remote-tables:

Add remote tables
-----------------

Specifying the remoteCatalog and remoteSchema attributes on a table makes it a remote table and as such a logical table.

.. code-block:: xml
    :linenos:
    :emphasize-lines: 3

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <tables>
            <table name="CONTRACT" remoteCatalog="other" remoteSchema="other">
                <column name="contractId" autoUpdated="true" primaryKey="true" type="INT"/>
                <column name="accountId" type="INT">
                    <foreignKey table="ACCOUNT" column="accountId"/>
                </column>
                <column name="agentId" type="INT">
                    <foreignKey table="AGENT" column="aId"/>
                </column>
            </table>
        </tables>
    </schemaMeta>

.. _schemameta-add-columns:

Add columns
-----------

.. code-block:: xml
    :linenos:
    :emphasize-lines: 4

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <tables>
            <table name="ACCOUNT">
                <column name="this_is_new" type="INT" />
            </table>
        </tables>
    </schemaMeta>

.. _schemameta-disableImpliedKeys:

Exclude columns from implied relationships
------------------------------------------

Explicitly disables relationships to or from
this column that may be implied by the column's
name, type and size.

Available options: to, from, all, none |br|
Default: none

.. code-block:: xml
    :linenos:
    :emphasize-lines: 4

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <tables>
            <table name="AGENT">
                <column name="accountId" type="INT" disableImpliedKeys="all"/>
            </table>
        </tables>
    </schemaMeta>

.. _schemameta-disableDiagramAssociations:

Exclude columns from diagrams
-----------------------------

Sometimes the associations displayed on a
relationships diagram cause the diagram to
become much more cluttered than it needs to be.
Enable this setting to not show the
relationships between this column and other
columns.

Use exceptDirect to disable associations on all
diagrams except for the diagrams of tables
directly (within one degree of separation)
connected to this column.

Available options: all, exceptDirect, none |br|
Defaults: none

.. code-block:: xml
    :linenos:
    :emphasize-lines: 4

    <schemaMeta xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://schemaspy.org/xsd/6/schemameta.xsd" >
        <tables>
            <table name="COUNTRY">
                <column name="countryId" type="INT" disableDiagramAssociations="all"/>
            </table>
        </tables>
    </schemaMeta>

.. |xsd_link| raw:: html

   <a href="http://schemaspy.org/xsd/6/schemameta.xsd" target="_blank">here</a>