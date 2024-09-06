#!/bin/bash

if [ ! -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="$JAVA_OPTS"
fi

if [ ! -z "$APP_CMD" ]; then
    COMMAND="$APP_CMD"
else
    COMMAND=""
fi

if [ ! -z "$APP_CMD_ARGS" ]; then
    ARGS="$APP_CMD_ARGS"
else
    ARGS=""
fi

echo "Running command: java $JAVA_OPTS -jar /app/91160-cli.jar $COMMAND $ARGS $@"

java $JAVA_OPTS -jar /app/91160-cli.jar $COMMAND $ARGS "$@"