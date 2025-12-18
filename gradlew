#!/bin/bash

# BSD-style readlink for macOS/Linux compatibility
DIR=$(dirname "$0")
cd "$DIR" || exit 1

# Execute Gradle
exec ./gradlew "$@"
