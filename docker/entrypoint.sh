#!/bin/sh
export MAIN_CLASS=$(unzip -p schemaspy*.jar META-INF/MANIFEST.MF | grep Main-Class | awk -F ': ' '{sub(/\r/,"",$2);print $2}')
[ "$(ls -A /drivers)" ] && export DRIVER_PATH=/drivers/ || export DRIVER_PATH=/drivers_inc/
echo "Running Main-Class $MAIN_CLASS"
echo -n "With drivers:"
ls -Ax $DRIVER_PATH | sed -e 's/  */, /g'
exec java -cp *:$DRIVER_PATH* $MAIN_CLASS -o /output "$@"