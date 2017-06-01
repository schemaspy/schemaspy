#!/bin/sh
source /.env
exec java -cp *:/drivers/* $MAIN_CLASS -o /output "$@"