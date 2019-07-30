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


usage() { echo "Usage: $0 -r 1.0-alpha.1 (specify release version) -d 1.0-alpha.2-SNAPSHOT (specify development version)" 1>&2; exit 1; }

prompt_confirm() {
  while true; do
    read -r -n 1 -p "${1:-Continue?} [y/n]: " REPLY
    case $REPLY in
      [yY]) echo ; return 0 ;;
      [nN]) echo ; return 1 ;;
      *) printf " \033[31m %s \n\033[0m" "invalid input"
    esac
  done
}

SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"

while getopts ":r:d:" o; do
    case "${o}" in
        r)
            releaseVersion=${OPTARG}
            ;;
        d)
            developmentVersion=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${releaseVersion}" ] || [ -z "${developmentVersion}" ]; then
    usage
fi

echo
echo "Preparing release" ${releaseVersion} "with tag v"${releaseVersion} "and next development version" ${developmentVersion} "..."
echo

prompt_confirm "Do you want continue?" || exit 0

cd ${SCRIPT_PATH}/..


#
# Update POM version
#
mvn org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false

prompt_confirm "Mark ${releaseVersion} as released in CHANGELOG.md" || exit 0
prompt_confirm "Update versions in README.md to ${releaseVersion}" || exit 0

git add --all
git commit -am "release influxdb-client-${releaseVersion}"

echo
prompt_confirm "Do you want push release ${releaseVersion}?" || exit 0

git tag -a ${releaseVersion} -m "Created tag for release: ${releaseVersion}"
git push origin ${releaseVersion}

#
# Update POM version
#
mvn org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=${developmentVersion} -DgenerateBackupPoms=false

echo

echo "Prepared the next development iteration ${developmentVersion}!"
echo
echo "Next steps"
echo
echo "  1. wait for finish build on Travis CI: https://travis-ci.org/influxdata/influxdb-client-java"
echo "  2. add ${developmentVersion} iteration to CHANGELOG.md"
echo "  3. commit changes: git commit -am \"prepare for next development iteration ${developmentVersion}\""
echo "  4. push changes: git push origin master"

