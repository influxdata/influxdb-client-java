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



SCRIPT_PATH="$( cd "$(dirname "$0")" ; pwd -P )"

cd ${SCRIPT_PATH}/../client

mvn org.openapitools:openapi-generator-maven-plugin:generate

echo
echo "Generated sources"
echo

cd ${SCRIPT_PATH}/../client/src/generated/java/org/influxdata/client/domain

for f in *.java; do
   case ${f} in
      (Bucket.java|BucketLinks.java|BucketRetentionRules.java|Buckets.java|Label.java|LabelMapping.java|LabelResponse.java|Links.java|OperationLogs.java|OperationLog.java|OperationLogLinks.java|User.java|UserLinks.java|Users.java|UsersLinks.java|ResourceMember.java|ResourceMembers.java|ResourceOwner.java|ResourceOwners.java|AddResourceMemberRequestBody.java|ScraperTargetRequest.java|ScraperTargetResponse.java|ScraperTargetResponses.java|ScraperTargetResponseLinks.java|Organization.java|OrganizationLinks.java|Organizations.java|LabelUpdate.java|LabelsResponse.java|Labels.java)
         ;;           # do nothing
      (*)
         rm -- "$f";; # remove the file
   esac
done