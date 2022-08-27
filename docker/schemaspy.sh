#!/bin/sh
[ -d $SCHEMASPY_DRIVERS ] && export DRIVER_PATH=$SCHEMASPY_DRIVERS || export DRIVER_PATH=/drivers_inc/
echo -n "Using drivers:"
ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'
exec java -jar /usr/local/lib/schemaspy/schemaspy*.jar \
  -t $DB_TYPE -dp $DRIVER_PATH -db $SQL_DATABASE \
  -host $SQL_HOST -port $SQL_PORT \
  -u $SQL_USER -p $SQL_PASSWORD \
  -o $SCHEMASPY_OUTPUT