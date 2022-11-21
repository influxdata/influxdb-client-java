#!/usr/bin/env bash
#
# The MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#


#
# How to run script from ROOT path:
#   docker run --rm -it -v "${PWD}":/code/client -v ~/.m2:/root/.m2 -w /code maven:3-openjdk-8 /code/client/scripts/generate-sources.sh
#

#
# Download customized generator
#
git clone --single-branch --branch master https://github.com/bonitoo-io/influxdb-clients-apigen "/code/influxdb-clients-apigen"
mkdir -p /code/influxdb-clients-apigen/build/
ln -s /code/client /code/influxdb-clients-apigen/build/influxdb-client-java
cd /code/influxdb-clients-apigen/ || exit

#
# Download APIs contracts
#
wget https://raw.githubusercontent.com/influxdata/openapi/master/contracts/oss.yml -O "/code/influxdb-clients-apigen/oss.yml"
wget https://raw.githubusercontent.com/influxdata/openapi/master/contracts/cloud.yml -O "/code/influxdb-clients-apigen/cloud.yml"
wget https://raw.githubusercontent.com/influxdata/openapi/master/contracts/invocable-scripts.yml -O "/code/influxdb-clients-apigen/invocable-scripts.yml"

#
# Build generator
#
mvn -DskipTests -f /code/influxdb-clients-apigen/openapi-generator/pom.xml clean install

#
# Prepare customized contract
#
mvn -f /code/influxdb-clients-apigen/openapi-generator/pom.xml compile exec:java -Dexec.mainClass="com.influxdb.AppendCloudDefinitions" -Dexec.args="oss.yml cloud.yml"
mvn -f /code/influxdb-clients-apigen/openapi-generator/pom.xml compile exec:java -Dexec.mainClass="com.influxdb.MergeContracts" -Dexec.args="oss.yml invocable-scripts.yml"
mvn -f /code/influxdb-clients-apigen/openapi-generator/pom.xml compile exec:java -Dexec.mainClass="com.influxdb.AppendCustomDefinitions" -Dexec.args="oss.yml --write-consistency"

#
# Generate sources
#
./generate-java.sh
