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

#!/usr/bin/env bash

set -e

SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"

# Generate Site
cd "${SCRIPT_PATH}"/..
mvn site site:stage -DskipTests
# Copy Kotlin doc
cp -R "${SCRIPT_PATH}"/../client-kotlin/target/dokka/ "${SCRIPT_PATH}"/../target/staging/influxdb-client-kotlin/dokka/

# Copy site
rm -rf "${HOME}"/site/*
cp -R "${SCRIPT_PATH}"/../target/staging/ "${HOME}"/site

# Clone GitHub pages
echo "Clone: gh-pages"
cd "${HOME}"
rm -rf "${HOME}"/gh-pages
git clone --depth 1 --branch=gh-pages git@github.com:influxdata/influxdb-client-java.git "${HOME}"/gh-pages

echo "Copy site"
# Push Site
rm -rf "${HOME}"/gh-pages/*
cd "${HOME}"/gh-pages
ls
cp -Rf "${HOME}"/site/* ./
ls

echo "Copy CircleCI"
mkdir "${HOME}"/gh-pages/.circleci/ || true
cp "${SCRIPT_PATH}"/../.circleci/config.yml "${HOME}"/gh-pages/.circleci/config.yml


echo "Commit"
git add -f .
git commit -m "Pushed the latest Maven site to GitHub pages [skip CI]"
git push -fq origin gh-pages > /dev/null
