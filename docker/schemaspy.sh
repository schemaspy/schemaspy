#!/bin/sh

if [ x"${SCHEMASPY_RUN_WHEN_EXISTS}" == "x" ]; then 
     echo "SCHEMASPY_RUN_WHEN_EXISTS not set"
else
  if [ -f "$SCHEMASPY_RUN_WHEN_EXISTS" ]; then
     echo "$SCHEMASPY_RUN_WHEN_EXISTS exists, running..."
  else
     echo "$SCHEMASPY_RUN_WHEN_EXISTS not found"
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

if [ -f "$SCHEMASPY_RUN_WHEN_EXISTS" ]; then
  rm $SCHEMASPY_RUN_WHEN_EXISTS
fi    