#!/bin/sh

if [ x"${RUN_WHEN_EXISTS}" == "x" ]; then 
     echo "RUN_WHEN_EXISTS not set"
else
  if [ -f "$RUN_WHEN_EXISTS" ]; then
     echo "$RUN_WHEN_EXISTS exists, running..."
  else
     echo "$RUN_WHEN_EXISTS not found"
     exit 1
  fi   
fi

[ -d $SCHEMASPY_DRIVERS ] && export DRIVER_PATH=$SCHEMASPY_DRIVERS || export DRIVER_PATH=/drivers_inc/
echo -n "Using drivers:"
ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'
exec java -jar /usr/local/lib/schemaspy/schemaspy*.jar \
  -t $DB_TYPE -dp $DRIVER_PATH -db $SQL_DATABASE \
  -host $SQL_HOST -port $SQL_PORT \
  -u $SQL_USER -p $SQL_PASSWORD \
  -o $SCHEMASPY_OUTPUT