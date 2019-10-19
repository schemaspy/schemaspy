#!/bin/sh
[ -d $DRIVERS ] && export DRIVER_PATH=$DRIVERS || export DRIVER_PATH=/drivers_inc/
echo -n "Using drivers:"
ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'
exec java -jar /usr/local/lib/schemaspy/schemaspy*.jar -dp $DRIVER_PATH -o $OUTPUT "$@"