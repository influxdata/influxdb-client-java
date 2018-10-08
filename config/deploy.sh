#!/usr/bin/env bash

set -e

## deploy artifacts to maven repository
mvn clean deploy -DskipTests -s config/deploy-settings.xml

## publish maven site go github.io
./config/publish-site.sh
