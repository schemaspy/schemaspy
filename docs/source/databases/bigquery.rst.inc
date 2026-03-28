Google BigQuery
===============

SchemaSpy supports Google BigQuery using the official Google Cloud BigQuery JDBC driver.

Prerequisites
-------------

1. **JDBC Driver**: Download the Google Cloud BigQuery JDBC driver from:
   
   - Official Google driver: https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers
   - Maven Central: ``com.google.cloud:google-cloud-bigquery``

2. **Google Cloud Project**: You need access to a Google Cloud Project with BigQuery enabled

3. **Authentication**: Set up authentication using one of these methods:
   
   - Service Account JSON key file
   - Application Default Credentials (ADC)
   - OAuth 2.0 user authentication

Configuration
-------------

Basic Command
~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t bigquery \
        -dp /path/to/google-cloud-bigquery-jdbc.jar \
        -cat YOUR_PROJECT_ID \
        -s YOUR_DATASET \
        -o output_directory \
        -u YOUR_EMAIL

Using Service Account Authentication
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: bash

    java -jar schemaspy.jar \
        -t bigquery \
        -dp /path/to/google-cloud-bigquery-jdbc.jar \
        -cat YOUR_PROJECT_ID \
        -s YOUR_DATASET \
        -o output_directory \
        -connprops "OAuthType\;0;OAuthServiceAcctEmail\;your-service-account@project.iam.gserviceaccount.com;OAuthPvtKeyPath\;/path/to/service-account-key.json"

Configuration File Example
~~~~~~~~~~~~~~~~~~~~~~~~~

Create a ``schemaspy.properties`` file:

.. code-block:: properties

    # Database type
    schemaspy.t=bigquery
    
    # JDBC driver path
    schemaspy.dp=/path/to/google-cloud-bigquery-jdbc.jar
    
    # Google Cloud Project ID
    schemaspy.cat=your-project-id
    
    # BigQuery dataset (schema)
    schemaspy.s=your_dataset
    
    # Output directory
    schemaspy.o=./output
    
    # User email (optional)
    schemaspy.u=your-email@example.com
    
    # Connection properties for service account authentication
    # Format: key\;value;key\;value
    schemaspy.connprops=OAuthType\;0;OAuthServiceAcctEmail\;your-sa@project.iam.gserviceaccount.com;OAuthPvtKeyPath\;/path/to/key.json

Authentication Options
---------------------

Service Account (OAuthType=0)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Recommended for automated/CI/CD environments:

.. code-block:: properties

    schemaspy.connprops=OAuthType\;0;OAuthServiceAcctEmail\;your-sa@project.iam.gserviceaccount.com;OAuthPvtKeyPath\;/path/to/service-account-key.json

Application Default Credentials (OAuthType=3)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Uses Google Cloud SDK credentials:

.. code-block:: properties

    schemaspy.connprops=OAuthType\;3

User Authentication (OAuthType=1)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For interactive use:

.. code-block:: properties

    schemaspy.connprops=OAuthType\;1

Connection URL Format
--------------------

The BigQuery connection URL follows this pattern:

.. code-block:: text

    jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<project>;DefaultDataset=<dataset>;

Additional connection parameters can be appended with semicolons.

Important Notes
--------------

1. **Datasets as Schemas**: In BigQuery, datasets are treated as schemas in SchemaSpy.

2. **Project as Catalog**: The Google Cloud Project ID is used as the catalog.

3. **Row Counts**: Row counts are approximate and retrieved from BigQuery's metadata tables (``__TABLES__``).

4. **Limited Constraint Support**: BigQuery has limited support for traditional database constraints:
   
   - No foreign keys (relationships must be inferred or specified via schemaMeta.xml)
   - No check constraints in the traditional sense
   - No sequences

5. **Information Schema**: BigQuery provides metadata through INFORMATION_SCHEMA views.

6. **Partitioned and Clustered Tables**: These are visible as regular tables in the documentation.

Example Usage
-------------

Full example with all options:

.. code-block:: bash

    java -jar schemaspy.jar \
        -t bigquery \
        -dp google-cloud-bigquery-jdbc.jar \
        -cat my-gcp-project \
        -s my_dataset \
        -o ./bigquery-docs \
        -u user@example.com \
        -connprops "OAuthType\;0;OAuthServiceAcctEmail\;schemaspy@my-project.iam.gserviceaccount.com;OAuthPvtKeyPath\;/keys/service-account.json" \
        -norows \
        -vizjs

Troubleshooting
--------------

**Connection Issues**

If you encounter connection errors:

1. Verify your Google Cloud Project ID is correct
2. Ensure the dataset exists in the specified project
3. Check that authentication credentials are valid
4. Verify the service account has BigQuery Data Viewer role (or higher)

**Missing Tables**

If tables don't appear:

1. Verify the dataset name is correct (case-sensitive)
2. Check user/service account permissions
3. Ensure tables exist in the specified dataset

**Performance**

For large datasets:

1. Use the ``-norows`` flag to skip row counting
2. Consider analyzing one dataset at a time
3. Use ``-vizjs`` to avoid Graphviz installation

Limitations
----------

1. **No Foreign Key Relationships**: BigQuery doesn't enforce foreign key constraints, so relationships won't be automatically detected. You can define them using a ``schemaMeta.xml`` file.

2. **No Indexes**: BigQuery doesn't have traditional indexes (it uses automatic optimization).

3. **Approximate Row Counts**: Row counts are estimates from metadata, not exact counts.

4. **Routine Details**: Some routine details may be limited compared to traditional databases.

Resources
---------

- `BigQuery Documentation <https://cloud.google.com/bigquery/docs>`_
- `BigQuery JDBC Driver Documentation <https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers>`_
- `BigQuery Information Schema <https://cloud.google.com/bigquery/docs/information-schema-intro>`_
- `SchemaSpy Configuration Guide <https://schemaspy.readthedocs.io/en/latest/configuration/databaseType.html>`_
