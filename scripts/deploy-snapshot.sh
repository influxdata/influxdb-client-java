#!/usr/bin/env bash

set -e

#Parse project version from pom.xml
export PROJECT_VERSION=$(xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" pom.xml)
echo "Project version: $PROJECT_VERSION"

#Skip if not *SNAPSHOT
if [[ $PROJECT_VERSION != *SNAPSHOT ]]; then
    echo "$PROJECT_VERSION is not SNAPSHOT - skip deploy.";
    exit;
fi

mvn clean deploy -DskipTests -s scripts/deploy-settings.xml
