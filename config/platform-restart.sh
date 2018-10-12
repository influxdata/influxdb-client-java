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

DEFAULT_DOCKER_REGISTRY="quay.io/influxdb/"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-$DEFAULT_DOCKER_REGISTRY}"

DEFAULT_INFLUXDB_VERSION="nightly"
INFLUXDB_VERSION="${INFLUXDB_VERSION:-$DEFAULT_INFLUXDB_VERSION}"
INFLUXDB_IMAGE=${DOCKER_REGISTRY}influxdb:${INFLUXDB_VERSION}-alpine

DEFAULT_INFLUX_PLATFORM_VERSION="nightly"
INFLUX_PLATFORM_VERSION="${INFLUX_PLATFORM_VERSION:-$DEFAULT_INFLUX_PLATFORM_VERSION}"
INFLUX_PLATFORM_IMAGE=${DOCKER_REGISTRY}influx:${INFLUX_PLATFORM_VERSION}

SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"

echo
echo "Restarting InfluxDB [${INFLUXDB_IMAGE}] ..."
echo

#
# InfluxDB
#

docker kill influxdb || true
docker rm influxdb || true
docker pull ${INFLUXDB_IMAGE} || true
docker run \
       --detach \
       --name influxdb \
       --publish 8086:8086 \
       --publish 8089:8089/udp \
       --volume ${SCRIPT_PATH}/influxdb.conf:/etc/influxdb/influxdb.conf \
       ${INFLUXDB_IMAGE}

echo "Wait 5s to start InfluxDB"
sleep 5

#
# InfluxData Platform
#
echo
echo "Restarting InfluxData Platform [${INFLUX_PLATFORM_IMAGE}] ... "
echo

docker kill influxdata-platform || true
docker rm influxdata-platform || true
docker pull ${INFLUX_PLATFORM_IMAGE} || true
docker run \
       --detach \
       --name influxdata-platform \
       --link=influxdb \
       --publish 9999:9999 \
       ${INFLUX_PLATFORM_IMAGE}

echo "Wait 5s to start InfluxData Platform"
sleep 5

echo
echo "Post onBoarding request, to setup initial user (my-user@my-password), org (my-org) and bucketSetup (my-bucket)"
echo
curl -i -X POST http://localhost:9999/api/v2/setup -H 'accept: application/json' \
    -d '{
            "username": "my-user",
            "password": "my-password",
            "org": "my-org",
            "bucket": "my-bucket"
        }'




