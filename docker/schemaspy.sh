#!/bin/sh

run_schemaspy() {
    [ -d $SCHEMASPY_DRIVERS ] && export DRIVER_PATH=$SCHEMASPY_DRIVERS || export DRIVER_PATH=/drivers_inc/
    echo -n "Using drivers:"
    ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'
    exec java -jar /usr/local/lib/schemaspy/schemaspy*.jar -dp $DRIVER_PATH -o $SCHEMASPY_OUTPUT "$@"
}

echo "Entrypoint script params:"
echo "$@"
# If the first argument to entrypoint starts with dash
if echo "$1" | grep -qE '^-'
then
	run_schemaspy
else
	exec "$@"
fi
