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

set -e

DEFAULT_MAVEN_JAVA_VERSION="3-jdk-8-slim"
MAVEN_JAVA_VERSION="${MAVEN_JAVA_VERSION:-$DEFAULT_MAVEN_JAVA_VERSION}"

SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"

${SCRIPT_PATH}/platform-restart.sh

ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'
INFLUXDB_IP=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | grep 10`
FLUX_IP=${INFLUXDB_IP}
PLATFORM_IP=${INFLUXDB_IP}

test -t 1 && USE_TTY="-t"

docker run -i ${USE_TTY} --rm \
       --volume ${SCRIPT_PATH}/../:/usr/src/mymaven \
       --volume ${SCRIPT_PATH}/../.m2:/root/.m2 \
       --workdir /usr/src/mymaven \
       --link=influxdb \
       --link=influxdata-platform \
       --env INFLUXDB_VERSION=${INFLUXDB_VERSION} \
       --env INFLUXDB_IP=${INFLUXDB_IP} \
       --env FLUX_IP=${FLUX_IP} \
       --env PLATFORM_IP=${PLATFORM_IP} \
       maven:${MAVEN_JAVA_VERSION} mvn clean install -U

docker kill influxdb || true
docker kill influxdata-platform || true