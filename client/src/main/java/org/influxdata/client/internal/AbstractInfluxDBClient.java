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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.Arguments;
import org.influxdata.client.InfluxDBClientOptions;
import org.influxdata.client.domain.Health;
import org.influxdata.client.internal.annotations.ToNullJson;
import org.influxdata.exceptions.InfluxException;
import org.influxdata.internal.AbstractRestClient;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (20/11/2018 07:13)
 */
public abstract class AbstractInfluxDBClient<T> extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractInfluxDBClient.class.getName());

    public final T influxDBService;

    final Moshi moshi;

    protected final HttpLoggingInterceptor loggingInterceptor;
    protected final GzipInterceptor gzipInterceptor;
    private final AuthenticateInterceptor authenticateInterceptor;
    private final OkHttpClient okHttpClient;

    public AbstractInfluxDBClient(@Nonnull final InfluxDBClientOptions options, @Nonnull final Class<T> serviceType) {

        Arguments.checkNotNull(options, "InfluxDBClientOptions");
        Arguments.checkNotNull(serviceType, "InfluxDB service type");

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

        this.moshi = new Moshi.Builder()
                .add(Instant.class, new InstantAdapter())
                //
                // Unknown enum value to null
                //
                .add(new NullToEmptyStringAdapter())
                .add(new JsonAdapter.Factory() {
                    @Nullable
                    @Override
                    public JsonAdapter create(@Nonnull final Type type,
                                              @Nonnull final Set<? extends Annotation> annotations,
                                              @Nonnull final Moshi moshi) {

                        Class rawType = Types.getRawType(type);
                        if (rawType.isEnum()) {
                            //noinspection unchecked
                            return new EnumJsonAdapter(rawType);
                        }

                        return null;
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(this.moshi).asLenient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        this.influxDBService = retrofit.create(serviceType);
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
            } else {
                writer.nullValue();
            }
        }
    }

    /**
     * EnumJsonAdapter is able to skip unknown enum values.
     * Inspirited from com.squareup.moshi.StandardJsonAdapters.EnumJsonAdapter.
     * <p>
     * TODO use 1.8 Moshi Adapter after upgrade Retrofit
     */
    private final class EnumJsonAdapter extends JsonAdapter<Enum> {

        private final Class enumType;
        private final String[] nameStrings;
        private final Enum[] constants;
        private final JsonReader.Options options;

        EnumJsonAdapter(final Class<Enum> enumType) {
            this.enumType = enumType;
            try {
                constants = enumType.getEnumConstants();
                nameStrings = new String[constants.length];
                for (int i = 0; i < constants.length; i++) {
                    Enum constant = constants[i];
                    Json annotation = enumType.getField(constant.name()).getAnnotation(Json.class);
                    String name = annotation != null ? annotation.name() : constant.name();
                    nameStrings[i] = name;
                }
                options = JsonReader.Options.of(nameStrings);
            } catch (NoSuchFieldException e) {
                throw new AssertionError("Missing field in " + enumType.getName(), e);
            }
        }

        @Override
        public Enum fromJson(final JsonReader reader) throws IOException {
            int index = reader.selectString(options);
            if (index != -1) {
                return constants[index];
            }

            String name = reader.nextString();

            LOG.log(Level.WARNING, "Expected one of {0} but was {1} at path {2}",
                    new Object[]{Arrays.asList(nameStrings), name, reader.getPath()});

            return null;
        }

        @Override
        public void toJson(final JsonWriter writer, final Enum value) throws IOException {
            writer.value(nameStrings[value.ordinal()]);
        }

        @Override
        public String toString() {
            return "EnumJsonAdapter(" + enumType.getName() + ")";
        }
    }

    private final class NullToEmptyStringAdapter {

        @ToJson
        public String toJson(@SuppressWarnings("unused") @Nullable @ToNullJson final String value) {
            return null;
        }

        @FromJson
        @ToNullJson
        public String fromJson(@Nullable final String data) {
            return data;
        }
    }
}