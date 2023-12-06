#!/bin/sh

cd $(dirname $0) || exit 1

# Build
mvn package || exit 1

# Run
java --enable-preview --enable-native-access=ALL-UNNAMED -cp target/panama.jar me.bechberger.panama.ErrnoExample