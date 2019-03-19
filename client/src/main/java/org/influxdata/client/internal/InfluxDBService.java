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
package org.influxdata.client.internal;

import java.util.Map;
import javax.annotation.Nonnull;

import org.influxdata.client.domain.Check;
import org.influxdata.client.domain.IsOnboarding;
import org.influxdata.client.domain.OnboardingResponse;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.User;
import org.influxdata.client.domain.Users;

import io.reactivex.Maybe;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
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
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 13:30)
 */
public interface InfluxDBService {

    //
    // Health
    //
    @GET("/health")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Check> health();

    //
    // Ready
    //
    @GET("/ready")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Check> ready();

    //
    // Setup
    //
    @GET("/api/v2/setup")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<IsOnboarding> setup();

    @POST("/api/v2/setup")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<OnboardingResponse> setup(@Nonnull @Body final RequestBody onboarding);

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
                                  //TODO set user password -> https://github.com/influxdata/influxdb/issues/11590
                                  @Nonnull @Header("Authorization") final String credentials,
                                  @Nonnull @Body final RequestBody password);

    @GET("/api/v2/users/{id}/logs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<OperationLogs> findUserLogs(@Nonnull @Path("id") final String userID,
                                           @Nonnull @QueryMap final Map<String, Object> findOptions);

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



    // Write
    //
    @POST("/api/v2/write")
    @Nonnull
    Maybe<Response<Void>> writePoints(@Query("org") final String orgID,
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
    Call<ResponseBody> query(@Query("orgID") final String orgID, @Nonnull @Body final RequestBody query);

}