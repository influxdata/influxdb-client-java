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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Buckets;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Organizations;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.domain.UserResourceMapping;
import org.influxdata.platform.domain.Users;

import io.reactivex.Completable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 13:30)
 */
interface PlatformService {

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
    Call<List<UserResourceMapping>> findOrganizationMembers(@Nonnull @Path("id") final String organizationID);

    @POST("/api/v2/orgs/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<UserResourceMapping> addOrganizationMember(@Nonnull @Path("id") final String organizationID,
                                                    @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/orgs/{id}/members/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteOrganizationMember(@Nonnull @Path("id") final String organizationID,
                                        @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/orgs/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<UserResourceMapping>> findOrganizationOwners(@Nonnull @Path("id") final String organizationID);

    @POST("/api/v2/orgs/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<UserResourceMapping> addOrganizationOwner(@Nonnull @Path("id") final String organizationID,
                                                   @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/orgs/{id}/owners/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteOrganizationOwner(@Nonnull @Path("id") final String organizationID,
                                       @Nonnull @Path("userID") final String userID);

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
    Call<List<UserResourceMapping>> findBucketMembers(@Nonnull @Path("id") final String bucketID);

    @POST("/api/v2/buckets/{id}/members")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<UserResourceMapping> addBucketMember(@Nonnull @Path("id") final String bucketID,
                                              @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/buckets/{id}/members/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteBucketMember(@Nonnull @Path("id") final String bucketID,
                                  @Nonnull @Path("userID") final String userID);

    @GET("/api/v2/buckets/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<UserResourceMapping>> findBucketOwners(@Nonnull @Path("id") final String bucketID);

    @POST("/api/v2/buckets/{id}/owners")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<UserResourceMapping> addBucketOwner(@Nonnull @Path("id") final String bucketID,
                                             @Nonnull @Body final RequestBody member);

    @DELETE("/api/v2/buckets/{id}/owners/{userID}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Void> deleteBucketOwner(@Nonnull @Path("id") final String bucketID,
                                 @Nonnull @Path("userID") final String userID);

    //
    // Write
    //
    @POST("/api/v2/write")
    @Nonnull
    Completable writePoints(@Query("org") final String org,
                            @Query("bucket") final String bucket,
                            @Query("precision") final String precision,
                            @Body final RequestBody points);

}