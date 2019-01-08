/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.influxdata.platform.impl;

import java.time.Instant;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Authorizations;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Buckets;
import org.influxdata.platform.domain.Health;
import org.influxdata.platform.domain.OperationLogResponse;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Organizations;
import org.influxdata.platform.domain.Ready;
import org.influxdata.platform.domain.ResourceMember;
import org.influxdata.platform.domain.ResourceMembers;
import org.influxdata.platform.domain.Run;
import org.influxdata.platform.domain.RunsResponse;
import org.influxdata.platform.domain.Secrets;
import org.influxdata.platform.domain.Source;
import org.influxdata.platform.domain.Sources;
import org.influxdata.platform.domain.Task;
import org.influxdata.platform.domain.Tasks;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.domain.Users;

import io.reactivex.Completable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 13:30)
 */
interface PlatformService {

    //
    // Health
    //
    @GET("/health")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Health> health();

    //
    // Ready
    //
    @GET("/ready")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Ready> ready();

    //
    // User
    //
    @POST("/api/v2/users")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> createUser(@Nonnull @Body final RequestBody user);

    @DELETE("/api/v2/users/{id}")
    Call<Void> deleteUser(@Nonnull @Path("id") final String userID);

    @PATCH("/api/v2/users/{id}")
    Call<User> updateUser(@Nonnull @Path("id") final String userID, @Nonnull @Body final RequestBody user);

    @PUT("/api/v2/users/{id}/password")
    Call<User> updateUserPassword(@Nonnull @Path("id") final String userID,
                                  @Nonnull @Header("Authorization") final String credentials,
                                  @Nonnull @Body final RequestBody password);

    @GET("/api/v2/users/{id}/log")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<OperationLogResponse> findUserLogs(@Nonnull @Path("id") final String userID);

    @GET("/api/v2/users/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> findUserByID(@Nonnull @Path("id") final String userID);

    @GET("/api/v2/users/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Users> findUsers();

    @GET("/api/v2/me")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> me();

    @PUT("/api/v2/me/password")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> mePassword(@Nonnull @Header("Authorization") final String credentials,
                          @Nonnull @Body final RequestBody password);

    //
    // Organizations
    //
    @POST("/api/v2/orgs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organization> createOrganization(@Nonnull @Body final RequestBody organization);

    @DELETE("/api/v2/orgs/{id}")
    Call<Void> deleteOrganization(@Nonnull @Path("id") final String organizationID);

    @PATCH("/api/v2/orgs/{id}")
    Call<Organization> updateOrganization(@Nonnull @Path("id") final String organizationID,
                                          @Nonnull @Body final RequestBody organization);

    @GET("/api/v2/orgs/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organization> findOrganizationByID(@Nonnull @Path("id") final String organizationID);

    @GET("/api/v2/orgs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organizations> findOrganizations();

    @GET("/api/v2/orgs/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findOrganizationMembers(@Nonnull @Path("id") final String organizationID);

    @POST("/api/v2/orgs/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addOrganizationMember(@Nonnull @Path("id") final String organizationID,
                                               @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/orgs/{id}/members/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteOrganizationMember(@Nonnull @Path("id") final String organizationID,
                                        @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/orgs/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findOrganizationOwners(@Nonnull @Path("id") final String organizationID);

    @POST("/api/v2/orgs/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addOrganizationOwner(@Nonnull @Path("id") final String organizationID,
                                              @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/orgs/{id}/owners/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteOrganizationOwner(@Nonnull @Path("id") final String organizationID,
                                       @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/orgs/{id}/secrets")
    Call<Secrets> getSecrets(@Nonnull @Path("id") final String organizationID);

    @PATCH("/api/v2/orgs/{id}/secrets")
    Call<Void> putSecrets(@Nonnull @Path("id") final String organizationID, @Nonnull @Body final RequestBody secrets);

    @POST("/api/v2/orgs/{id}/secrets/delete")
    Call<Void> deleteSecrets(@Nonnull @Path("id") final String organizationID,
                             @Nonnull @Body final RequestBody secrets);

    //
    // Bucket
    //
    @POST("/api/v2/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Bucket> createBucket(@Nonnull @Body final RequestBody bucket);

    @DELETE("/api/v2/buckets/{id}")
    Call<Void> deleteBucket(@Nonnull @Path("id") final String bucketID);

    @PATCH("/api/v2/buckets/{id}")
    Call<Bucket> updateBucket(@Nonnull @Path("id") final String bucketID, @Nonnull @Body final RequestBody bucket);

    @GET("/api/v2/buckets/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Bucket> findBucketByID(@Nonnull @Path("id") final String bucketID);

    @GET("/api/v2/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Buckets> findBuckets(@Nullable @Query("org") final String organizationName);

    @GET("/api/v2/buckets/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findBucketMembers(@Nonnull @Path("id") final String bucketID);

    @POST("/api/v2/buckets/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addBucketMember(@Nonnull @Path("id") final String bucketID,
                                         @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/buckets/{id}/members/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteBucketMember(@Nonnull @Path("id") final String bucketID,
                                  @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/buckets/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findBucketOwners(@Nonnull @Path("id") final String bucketID);

    @POST("/api/v2/buckets/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addBucketOwner(@Nonnull @Path("id") final String bucketID,
                                        @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/buckets/{id}/owners/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteBucketOwner(@Nonnull @Path("id") final String bucketID,
                                 @Nonnull @Path("userID") final String userID);

    //
    // Authorization
    //
    @POST("/api/v2/authorizations")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorization> createAuthorization(@Nonnull @Body final RequestBody authorization);

    @DELETE("/api/v2/authorizations/{id}")
    Call<Void> deleteAuthorization(@Nonnull @Path("id") final String authorizationID);

    @PATCH("/api/v2/authorizations/{id}")
    Call<Authorization> updateAuthorization(@Nonnull @Path("id") final String authorizationID,
                                            @Nonnull @Body final RequestBody authorization);

    @GET("/api/v2/authorizations/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorizations> findAuthorizations(@Nullable @Query("userID") final String userID,
                                            @Nullable @Query("user") final String user);

    @GET("/api/v2/authorizations/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorization> findAuthorization(@Nonnull @Path("id") final String authorizationID);


    //
    // Source
    //
    @POST("/api/v2/sources")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Source> createSource(@Nonnull @Body final RequestBody source);

    @DELETE("/api/v2/sources/{id}")
    Call<Void> deleteSource(@Nonnull @Path("id") final String sourceID);

    @PATCH("/api/v2/sources/{id}")
    Call<Source> updateSource(@Nonnull @Path("id") final String sourceID,
                              @Nonnull @Body final RequestBody source);

    @GET("/api/v2/sources/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Source> findSource(@Nonnull @Path("id") final String sourceID);

    @GET("/api/v2/sources")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Sources> findSources();

    @GET("/api/v2/sources/{id}/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<Bucket>> findSourceBuckets(@Nonnull @Path("id") final String sourceID);

    @GET("/api/v2/sources/{id}/health")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Health> findSourceHealth(@Nonnull @Path("id") final String sourceID);

    //
    // Task
    //
    @POST("/api/v2/tasks")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Task> createTask(@Nonnull @Body final RequestBody task);

    @DELETE("/api/v2/tasks/{id}")
    Call<Void> deleteTask(@Nonnull @Path("id") final String taskID);

    @PATCH("/api/v2/tasks/{id}")
    Call<Task> updateTask(@Nonnull @Path("id") final String taskID, @Nonnull @Body final RequestBody task);

    @GET("/api/v2/tasks/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Tasks> findTasks(@Nullable @Query("after") final String after,
                          @Nullable @Query("user") final String user,
                          @Nullable @Query("organization") final String organization);

    @GET("/api/v2/tasks/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Task> findTaskByID(@Nonnull @Path("id") final String taskID);

    @GET("/api/v2/tasks/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findTaskMembers(@Nonnull @Path("id") final String taskID);

    @POST("/api/v2/tasks/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addTaskMember(@Nonnull @Path("id") final String taskID,
                                       @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/tasks/{id}/members/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteTaskMember(@Nonnull @Path("id") final String taskID,
                                @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/tasks/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMembers> findTaskOwners(@Nonnull @Path("id") final String taskID);

    @POST("/api/v2/tasks/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResourceMember> addTaskOwner(@Nonnull @Path("id") final String taskID,
                                      @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/tasks/{id}/owners/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteTaskOwner(@Nonnull @Path("id") final String taskID,
                               @Nonnull @Path("userID") final String userID);


    @GET("/api/v2/tasks/{id}/runs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<RunsResponse> findTaskRuns(@Nonnull @Path("id") final String taskID,
                                    @Nullable @Query("afterTime") final Instant afterTime,
                                    @Nullable @Query("beforeTime") final Instant beforeTime,
                                    @Nullable @Query("limit") final Integer limit,
                                    @Nonnull @Query("orgID") final String orgID);

    @GET("/api/v2/tasks/{id}/runs/{runID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Run> findTaskRun(@Nonnull @Path("id") final String taskID,
                          @Nonnull @Path("runID") final String runID);

    @GET("/api/v2/tasks/{id}/runs/{runID}/logs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<String>> findRunLogs(@Nonnull @Path("id") final String taskID,
                                   @Nonnull @Path("runID") final String runID,
                                   @Nonnull @Query("orgID") final String orgID);

    @POST("/api/v2/tasks/{id}/runs/{runID}/retry")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Run> retryTaskRun(@Nonnull @Path("id") final String taskID,
                           @Nonnull @Path("runID") final String runID);

    @DELETE("/api/v2/tasks/{id}/runs/{runID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> cancelRun(@Nonnull @Path("id") final String taskID,
                         @Nonnull @Path("runID") final String runID);

    @GET("/api/v2/tasks/{id}/logs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<String>> findTaskLogs(@Nonnull @Path("id") final String taskID,
                                    @Nonnull @Query("orgID") final String orgID);

    // Write
    //
    @POST("/api/v2/write")
    @Nonnull
    Completable writePoints(@Query("org") final String org,
                            @Query("bucket") final String bucket,
                            @Query("precision") final String precision,
                            @Body final RequestBody points);

    //
    // Query
    //
    @Streaming
    @POST("/api/v2/query")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResponseBody> query(@Query("organization") final String org, @Nonnull @Body final RequestBody query);

}