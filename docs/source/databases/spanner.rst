# Google Cloud Spanner

SchemaSpy supports Google Cloud Spanner databases through the Cloud Spanner JDBC driver.

## Prerequisites

1. **JDBC Driver**: Download the Cloud Spanner JDBC driver from Maven Central:
   ```
   https://repo1.maven.org/maven2/com/google/cloud/google-cloud-spanner-jdbc/
   ```

2. **Credentials**: Set up authentication using one of these methods:
   - Service Account JSON key file
   - Application Default Credentials (recommended for Google Cloud environments)
   - User credentials via `gcloud auth application-default login`

## Database Types

SchemaSpy provides two database type configurations for Spanner:

### `spanner`
Standard configuration with explicit credentials:
```bash
java -jar schemaspy.jar \
    -t spanner \
    -dp /path/to/google-cloud-spanner-jdbc.jar \
    -project your-project-id \
    -instance your-instance-id \
    -db your-database-name \
    -credentials /path/to/service-account.json \
    -o output-directory
```

### `spanner-adc`
Configuration using Application Default Credentials:
```bash
java -jar schemaspy.jar \
    -t spanner-adc \
    -dp /path/to/google-cloud-spanner-jdbc.jar \
    -project your-project-id \
    -instance your-instance-id \
    -db your-database-name \
    -o output-directory
```

## Parameters

| Parameter | Description | Required |
|-----------|-------------|----------|
| `project` | Google Cloud Project ID | Yes |
| `instance` | Cloud Spanner Instance ID | Yes |
| `db` | Database name within the instance | Yes |
| `credentials` | Path to service account JSON key file | No (if using ADC) |

## Connection Examples

### With Service Account Key
```properties
schemaspy.t=spanner
schemaspy.project=my-gcp-project
schemaspy.instance=my-spanner-instance
schemaspy.db=my-database
schemaspy.credentials=/path/to/service-account.json
schemaspy.dp=/path/to/google-cloud-spanner-jdbc.jar
schemaspy.o=spanner-output
```

### With Application Default Credentials
```properties
schemaspy.t=spanner-adc
schemaspy.project=my-gcp-project
schemaspy.instance=my-spanner-instance
schemaspy.db=my-database
schemaspy.dp=/path/to/google-cloud-spanner-jdbc.jar
schemaspy.o=spanner-output
```

## Notes

- Spanner uses its own SQL dialect. Some advanced SchemaSpy features may have limited support.
- The `-schema` parameter is optional for Spanner as it uses a flat namespace.
- Row count analysis (`-norows`) is recommended for large Spanner databases to improve performance.
- Spanner doesn't support traditional sequences; auto-generated primary keys use different mechanisms.

## Troubleshooting

### Authentication Errors
Ensure your credentials have the following IAM roles:
- `roles/spanner.databaseReader`
- `roles/spanner.viewer`

### Connection Timeouts
For large databases, consider:
- Using `-dbthreads 3` to reduce concurrent connections
- Adding `-norows` to skip row counting
- Limiting analysis with `-I` to exclude large tables

### JDBC Driver Issues
Make sure you're using a compatible JDBC driver version:
- For Java 8: Use driver version 6.x
- For Java 11+: Use the latest driver version
