#!/bin/sh

cd $(dirname $0) || exit 1

# Build
mvn package || exit 1

if [ -z "$1" ]; then
    echo "No example name supplied"
    exit 1
fi

# Run
java --enable-preview --enable-native-access=ALL-UNNAMED -cp target/panama.jar me.bechberger.panama.$1