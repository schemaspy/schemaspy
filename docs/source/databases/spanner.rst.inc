Google Cloud Spanner
====================

SchemaSpy supports Google Cloud Spanner using the official Google Cloud Spanner JDBC driver.

Prerequisites
-------------

1. **JDBC Driver**: Download the Google Cloud Spanner JDBC driver from:
   
   - Maven Central: ``com.google.cloud:google-cloud-spanner-jdbc``
   - https://cloud.google.com/spanner/docs/jdbc-drivers

2. **Google Cloud Project**: You need access to a Google Cloud Project with Cloud Spanner enabled

3. **Spanner Instance and Database**: Create a Cloud Spanner instance and database

4. **Authentication**: Set up authentication using one of these methods:
   
   - Service Account JSON key file
   - Application Default Credentials (ADC)
   - OAuth 2.0 user authentication

Configuration
-------------

Basic Command
~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t spanner \
        -dp /path/to/google-cloud-spanner-jdbc.jar \
        -cat YOUR_PROJECT_ID \
        -db YOUR_DATABASE \
        -s "" \
        -o output_directory \
        -connprops "instance\;YOUR_INSTANCE_ID"

Using Service Account Authentication
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t spanner \
        -dp /path/to/google-cloud-spanner-jdbc.jar \
        -cat YOUR_PROJECT_ID \
        -db YOUR_DATABASE \
        -s "" \
        -o output_directory \
        -connprops "instance\;YOUR_INSTANCE_ID;credentials\;/path/to/service-account-key.json"

Using Spanner Emulator (Local Development)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    # Start the emulator first:
    # docker run -p 9010:9010 -p 9020:9020 gcr.io/cloud-spanner-emulator/emulator

    java -jar schemaspy.jar \
        -t spanner \
        -dp /path/to/google-cloud-spanner-jdbc.jar \
        -cat test-project \
        -db test-database \
        -s "" \
        -o output_directory \
        -connprops "instance\;test-instance;autoConfigEmulator\;true"

Configuration File Example
~~~~~~~~~~~~~~~~~~~~~~~~~

Create a ``schemaspy.properties`` file:

.. code-block:: properties

    # Database type
    schemaspy.t=spanner
    
    # JDBC driver path
    schemaspy.dp=/path/to/google-cloud-spanner-jdbc.jar
    
    # Project ID (catalog)
    schemaspy.cat=YOUR_PROJECT_ID
    
    # Database name
    schemaspy.db=YOUR_DATABASE
    
    # Schema (use empty string for Spanner)
    schemaspy.s=
    
    # Output directory
    schemaspy.o=./output
    
    # Connection properties
    schemaspy.connprops=instance\\;YOUR_INSTANCE_ID;credentials\\;/path/to/key.json

Then run:

.. code-block:: bash

    java -jar schemaspy.jar -configFile schemaspy.properties

Important Notes
---------------

Schema Handling
~~~~~~~~~~~~~~~

Cloud Spanner does not have traditional schemas. All tables exist in a flat namespace within a database.
For SchemaSpy:

- Use ``-s ""`` (empty string) or ``-s YOUR_DATABASE`` for the schema parameter
- All tables will be shown in the same schema

Primary Keys
~~~~~~~~~~~~

- Every Cloud Spanner table **must** have a primary key
- Primary keys can be composite (multiple columns)
- SchemaSpy will correctly detect and display primary key information

Foreign Keys
~~~~~~~~~~~~

- Spanner supports foreign key constraints
- Foreign keys are enforced and will be displayed in the schema diagram
- SchemaSpy automatically detects foreign key relationships

Indexes
~~~~~~~

- Spanner supports secondary indexes for query optimization
- Indexes can be ascending (ASC) or descending (DESC)
- NULL_FILTERED and UNIQUE indexes are supported
- SchemaSpy will display all indexes with their properties

Interleaved Tables
~~~~~~~~~~~~~~~~~~

- Spanner's interleaved tables (parent-child relationships) are displayed as:
  - Regular tables with foreign key relationships
  - The interleaving is not explicitly shown but the relationships are captured

Performance
~~~~~~~~~~~

- Spanner does not provide exact row counts without full table scans
- SchemaSpy may show NULL for row counts to avoid expensive queries
- Consider running ``SELECT COUNT(*)`` manually for specific tables if needed

Authentication Methods
----------------------

Service Account (Recommended for CI/CD)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    -connprops "instance\;YOUR_INSTANCE;credentials\;/path/to/service-account.json"

Application Default Credentials (Local Development)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

First, set up ADC:

.. code-block:: bash

    gcloud auth application-default login

Then run SchemaSpy without explicit credentials:

.. code-block:: bash

    java -jar schemaspy.jar -t spanner -cat PROJECT -db DATABASE -s "" -connprops "instance\;INSTANCE"

OAuth 2.0 User Authentication
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Uses the standard OAuth 2.0 flow. Requires browser-based authentication.

Emulator (Testing)
~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    -connprops "instance\;test-instance;autoConfigEmulator\;true"

Connection String Format
------------------------

The JDBC URL format for Cloud Spanner:

.. code-block:: text

    jdbc:cloudspanner:/projects/PROJECT_ID/instances/INSTANCE_ID/databases/DATABASE_ID

Additional parameters can be appended:

- ``credentials=/path/to/key.json`` - Service account authentication
- ``autoConfigEmulator=true`` - Connect to local emulator
- ``minSessions=100`` - Connection pool configuration
- ``maxSessions=400`` - Maximum sessions in pool

Troubleshooting
---------------

Permission Denied
~~~~~~~~~~~~~~~~~

**Error**: ``Permission denied`` or ``403 Forbidden``

**Solutions**:

1. Verify IAM roles:
   
   - ``roles/spanner.databaseReader`` (minimum)
   - ``roles/spanner.viewer`` for instance metadata

2. Check authentication:
   
   .. code-block:: bash
   
       # Test ADC
       gcloud auth application-default print-access-token
   
       # Verify service account
       cat /path/to/service-account.json

Connection Timeout
~~~~~~~~~~~~~~~~~~

**Error**: ``Connection timeout`` or ``DEADLINE_EXCEEDED``

**Solutions**:

1. Verify network connectivity to ``spanner.googleapis.com``
2. Check firewall rules
3. For emulator, ensure it's running: ``docker ps | grep spanner``

Schema Not Found
~~~~~~~~~~~~~~~~

**Error**: ``Schema not found`` or ``Database not found``

**Solutions**:

1. Use empty string for schema: ``-s ""``
2. Verify database name matches exactly (case-sensitive)
3. Check project ID and instance ID are correct

Examples
--------

Basic Analysis
~~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t spanner \
        -dp spanner-jdbc.jar \
        -cat my-project \
        -db my-database \
        -s "" \
        -o ./output \
        -connprops "instance\;my-instance;credentials\;/path/to/key.json"

With Custom Connection Pool
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t spanner \
        -dp spanner-jdbc.jar \
        -cat my-project \
        -db my-database \
        -s "" \
        -o ./output \
        -connprops "instance\;my-instance;minSessions\;50;maxSessions\;200"

Using Emulator for Testing
~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    # Terminal 1: Start emulator
    docker run -p 9010:9010 -p 9020:9020 \
        gcr.io/cloud-spanner-emulator/emulator
    
    # Terminal 2: Run SchemaSpy
    java -jar schemaspy.jar \
        -t spanner \
        -dp spanner-jdbc.jar \
        -cat test-project \
        -db test-database \
        -s "" \
        -o ./output \
        -connprops "instance\;test-instance;autoConfigEmulator\;true"

Limitations
-----------

- **Row Counts**: Not available without expensive full table scans (shown as NULL)
- **Table Comments**: Spanner doesn't support table-level comments
- **Column Comments**: Spanner doesn't support column-level comments
- **Sequences**: Not applicable (Spanner uses internal mechanisms for auto-generation)
- **Stored Procedures**: Limited support (Spanner uses SQL functions instead)

Resources
---------

- `Cloud Spanner Documentation <https://cloud.google.com/spanner/docs>`_
- `JDBC Driver Documentation <https://cloud.google.com/spanner/docs/jdbc-drivers>`_
- `Spanner Emulator <https://cloud.google.com/spanner/docs/emulator>`_
- `IAM Roles for Spanner <https://cloud.google.com/spanner/docs/iam>`_
- `Schema Design Best Practices <https://cloud.google.com/spanner/docs/schema-design>`_
