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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.AuthorizationClient;
import org.influxdata.platform.BucketClient;
import org.influxdata.platform.OrganizationClient;
import org.influxdata.platform.PlatformClient;
import org.influxdata.platform.QueryClient;
import org.influxdata.platform.SourceClient;
import org.influxdata.platform.TaskClient;
import org.influxdata.platform.UserClient;
import org.influxdata.platform.WriteClient;
import org.influxdata.platform.option.PlatformOptions;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.rest.AbstractRestClient;
import org.influxdata.platform.rest.LogLevel;

import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (11/10/2018 09:36)
 */
public class PlatformClientImpl extends AbstractRestClient implements PlatformClient {

    private static final Logger LOG = Logger.getLogger(PlatformClientImpl.class.getName());

    private final Moshi moshi;
    private final PlatformService platformService;

    private final HttpLoggingInterceptor loggingInterceptor;
    private final AuthenticateInterceptor authenticateInterceptor;


    public PlatformClientImpl(@Nonnull final PlatformOptions options) {
        Arguments.checkNotNull(options, "PlatformOptions");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.authenticateInterceptor = new AuthenticateInterceptor(options);

        OkHttpClient okHttpClient = options.getOkHttpClient()
                .addInterceptor(this.loggingInterceptor)
                .addInterceptor(this.authenticateInterceptor)
                .build();

        this.authenticateInterceptor.initToken(okHttpClient);

        this.moshi = new Moshi.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(this.moshi).asLenient())
                .build();

        this.platformService = retrofit.create(PlatformService.class);
    }

    @Nonnull
    @Override
    public QueryClient createQueryClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public WriteClient createWriteClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions) {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public AuthorizationClient createAuthorizationClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public BucketClient createBucketClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public OrganizationClient createOrganizationClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public SourceClient createSourceClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public TaskClient createTaskClient() {
        throw new TodoException();
    }

    @Nonnull
    @Override
    public UserClient createUserClient() {
        return new UserClientImpl(platformService, moshi);
    }

    @Nonnull
    @Override
    public LogLevel getLogLevel() {
        return getLogLevel(this.loggingInterceptor);
    }

    @Nonnull
    @Override
    public PlatformClient setLogLevel(@Nonnull final LogLevel logLevel) {

        setLogLevel(this.loggingInterceptor, logLevel);

        return this;
    }

    @Override
    public void close() {

        //
        // signout
        //
        try {
            this.authenticateInterceptor.signout();
        } catch (IOException e) {
            LOG.log(Level.FINEST, "The signout exception", e);
        }
    }
}