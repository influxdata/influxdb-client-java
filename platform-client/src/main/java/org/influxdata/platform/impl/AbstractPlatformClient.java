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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.Arguments;
import org.influxdata.platform.domain.Health;
import org.influxdata.platform.error.InfluxException;
import org.influxdata.platform.option.PlatformOptions;
import org.influxdata.platform.rest.AbstractRestClient;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:13)
 */
abstract class AbstractPlatformClient<T> extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractPlatformClient.class.getName());

    final T platformService;

    final Moshi moshi;

    final HttpLoggingInterceptor loggingInterceptor;
    final GzipInterceptor gzipInterceptor;
    private final AuthenticateInterceptor authenticateInterceptor;
    private final OkHttpClient okHttpClient;

    AbstractPlatformClient(@Nonnull final PlatformOptions options, @Nonnull final Class<T> serviceType) {

        Arguments.checkNotNull(options, "PlatformOptions");
        Arguments.checkNotNull(serviceType, "Platform service type");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.authenticateInterceptor = new AuthenticateInterceptor(options);
        this.gzipInterceptor = new GzipInterceptor();

        this.okHttpClient = options.getOkHttpClient()
                .addInterceptor(this.loggingInterceptor)
                .addInterceptor(this.authenticateInterceptor)
                .addInterceptor(this.gzipInterceptor)
                .build();

        this.authenticateInterceptor.initToken(okHttpClient);

        this.moshi = new Moshi.Builder().add(Instant.class, new InstantAdapter()).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(this.moshi).asLenient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        this.platformService = retrofit.create(serviceType);
    }

    public void close() {

        //
        // signout
        //
        try {
            this.authenticateInterceptor.signout();
        } catch (IOException e) {
            LOG.log(Level.FINEST, "The signout exception", e);
        }

        //
        // Shutdown OkHttp
        //
        okHttpClient.connectionPool().evictAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }

    @Nonnull
    protected Health health(final Call<Health> healthCall) {

        Arguments.checkNotNull(healthCall, "health call");

        try {
            return execute(healthCall);
        } catch (InfluxException e) {
            Health health = new Health();
            health.setStatus("error");
            health.setMessage(e.getMessage());

            return health;
        }
    }

    private final class InstantAdapter extends JsonAdapter<Instant> {

        @Nullable
        @Override
        public Instant fromJson(final JsonReader reader) throws IOException {
            String value = reader.nextString();

            if (value == null) {
                return null;
            }

            try {
                return Instant.parse(value);
            } catch (DateTimeParseException e) {
                return DateTimeFormatter.ISO_DATE_TIME.parse(value, Instant::from);
            }
        }

        @Override
        public void toJson(@Nonnull final JsonWriter writer, @Nullable final Instant value) throws IOException {
            if (value != null) {
                writer.value(value.toString());
            }
        }
    }
}