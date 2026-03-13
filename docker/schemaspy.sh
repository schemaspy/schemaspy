#!/bin/sh

while [ $# -gt 0 ]; do
  case "$1" in
    -db)
      DB_NAME="$2"
      shift 2
      ;;
    -host)
      DB_HOST="$2"
      shift 2
      ;;
    -p)
      PASSWORD="$2"
      shift 2
      ;;
    -port)
      DB_PORT="$2"
      shift 2
      ;;
    -s)
      DB_SCHEMAS="$2"
      shift 2
      ;;
    -t)
      DB_TYPE="$2"
      shift 2
      ;;
    -u)
      USER="$2"
      shift 2
      ;;
  esac
done

JAVA_ARGS=""

if [ ! -z ${DB_TYPE} ]; then
  JAVA_ARGS="$JAVA_ARGS -t $DB_TYPE"
fi
if [ ! -z ${DB_NAME} ]; then
  JAVA_ARGS="$JAVA_ARGS -db $DB_NAME"
fi
if [ ! -z ${DB_HOST} ]; then
  JAVA_ARGS="$JAVA_ARGS -host $DB_HOST"
fi
if [ ! -z ${DB_PORT} ]; then
  JAVA_ARGS="$JAVA_ARGS -port $DB_PORT"
fi
if [ ! -z ${DB_SCHEMAS} ]; then
  JAVA_ARGS="$JAVA_ARGS -schemas $DB_SCHEMAS"
else
  JAVA_ARGS="$JAVA_ARGS -all"
fi
if [ ! -z ${USER} ]; then
  JAVA_ARGS="$JAVA_ARGS --user $USER"
fi
if [ ! -z ${PASSWORD} ]; then
  JAVA_ARGS="$JAVA_ARGS -p $PASSWORD"
fi

[ -d $SCHEMASPY_DRIVERS ] && export DRIVER_PATH=$SCHEMASPY_DRIVERS || export DRIVER_PATH=/drivers_inc/
echo -n "Using drivers:"
ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'

exec java -jar /usr/local/lib/schemaspy/schemaspy-app.jar -dp $DRIVER_PATH -o $SCHEMASPY_OUTPUT $JAVA_ARGS