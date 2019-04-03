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

# Generate OpenAPI generator
cd ${SCRIPT_PATH}/../openapi-generator/
mvn clean install -DskipTests

cd ${SCRIPT_PATH}/../client

# Generate client
mvn org.openapitools:openapi-generator-maven-plugin:generate

cd ${SCRIPT_PATH}/../client/src/generated/java/org/influxdata/client/domain

# Clear useless files

for f in *.java; do
   case ${f} in
      (Bucket.java|BucketLinks.java|BucketRetentionRules.java|Buckets.java|Label.java|LabelMapping.java|LabelResponse.java|Links.java|OperationLogs.java|OperationLog.java|OperationLogLinks.java|User.java|UserLinks.java|Users.java|UsersLinks.java|ResourceMember.java|ResourceMembers.java|ResourceOwner.java|ResourceOwners.java|AddResourceMemberRequestBody.java|ScraperTargetRequest.java|ScraperTargetResponse.java|ScraperTargetResponses.java|ScraperTargetResponseLinks.java|Organization.java|OrganizationLinks.java|Organizations.java|LabelUpdate.java|LabelsResponse.java|Labels.java|Source.java|SourceLinks.java|Sources.java|Check.java|OnboardingRequest.java|OnboardingResponse.java|IsOnboarding.java|Authorization.java|AuthorizationLinks.java|Authorizations.java|Permission.java|PermissionResource.java|SecretKeys.java|SecretKeysLinks.java|Task.java|TaskCreateRequest.java|TaskLinks.java|TaskUpdateRequest.java|LogEvent.java|Logs.java|Run.java|RunLinks.java|RunManually.java|Runs.java|Tasks.java|Error.java)
         ;;
      (Telegraf*.java|PasswordResetBody.java|LabelCreateRequest.java|WritePrecision.java|Document*.java|Variable.java|Variables.java|QueryVariableProperties.java|ConstantVariableProperties.java|MapVariableProperties.java|VariableLinks.java|SecretKeysResponse.java|SecretKeysResponseLinks.java|AuthorizationUpdateRequest.java|ProtoDashboard.java|MarkdownViewProperties.java|CreateCell.java|CellUpdate.java|Routes.java|RoutesExternal.java|RoutesQuery.java|RoutesSystem.java)
        ;;
      (Query*.java|LineProtocolError.java|LineProtocolLengthError.java|FluxSuggestions.java|LanguageRequest.java|AnalyzeQueryResponse.java|ASTResponse.java|Dialect.java|Field.java|FluxSuggestionsFuncs.java|AnalyzeQueryResponseErrors.java|ModelPackage.java|Ready.java|Protos.java|Dashboards.java|CreateProtoResourcesRequest.java|Proto.java|Dashboard.java|CreateDashboardRequest.java|Cells.java|DashboardLinks.java|DashboardMeta.java|ProtoLinks.java|View.java|Cell.java|ViewLinks.java|CellLinks.java|V1ViewProperties.java|EmptyViewProperties.java|LogViewProperties.java|LogViewerColumn.java|DashboardColor.java|DashboardQuery.java|RenamableField.java|V1ViewPropertiesAxes.java|V1ViewPropertiesDecimalPoints.java|V1ViewPropertiesLegend.java|LogViewerColumnSettings.java|DashboardQueryRange.java|Axis.java)
        ;;
      (*)
         rm -- "$f";; # remove the file
   esac
done
