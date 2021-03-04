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
package com.influxdb.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.Cancellable;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.internal.FluxCsvParser;
import com.influxdb.query.internal.FluxResultMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (17/10/201AbstractQueryApi8 11:20)
 */
public abstract class AbstractQueryApi extends AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractQueryApi.class.getName());

    protected final FluxCsvParser fluxCsvParser = new FluxCsvParser();
    protected final FluxResultMapper resultMapper = new FluxResultMapper();

    protected static final Runnable EMPTY_ACTION = () -> {

    };
    protected static final String DEFAULT_DIALECT;
    static {
        Map<String, Object> dialect = new HashMap<>();
        dialect.put("header", true);
        dialect.put("delimiter", ",");
        dialect.put("quoteChar", "\"");
        dialect.put("commentPrefix", "#");
        dialect.put("annotations", new String[]{"datatype", "group", "default"});
        DEFAULT_DIALECT = new GsonBuilder().create().toJson(dialect);
    }

    protected static final Consumer<Throwable> ERROR_CONSUMER = throwable -> {
        if (throwable instanceof InfluxException) {
            throw (InfluxException) throwable;
        } else {
            throw new InfluxException(throwable);
        }
    };

    @Nonnull
    protected RequestBody createBody(@Nullable final String dialect, @Nonnull final String query) {

        Arguments.checkNonEmpty(query, "Flux query");

        JsonObject json = new JsonObject();
        json.addProperty("query", query);

        if (dialect != null && !dialect.isEmpty()) {
            JsonElement dialectJson = new Gson().fromJson(dialect, JsonElement.class);
            json.add("dialect",  dialectJson);
        }

        return createBody(json.toString());
    }

    protected void query(@Nonnull final Call<ResponseBody> queryCall,
                         @Nonnull final FluxCsvParser.FluxResponseConsumer responseConsumer,
                         @Nonnull final Consumer<? super Throwable> onError,
                         @Nonnull final Runnable onComplete,
                         @Nonnull final Boolean asynchronously) {

        BiConsumer<Cancellable, BufferedSource> consumer = (cancellable, bufferedSource) -> {
            try {
                fluxCsvParser.parseFluxResponse(bufferedSource, cancellable, responseConsumer);
            } catch (IOException e) {
                onError.accept(e);
            }
        };

        query(queryCall, consumer, onError, onComplete, asynchronously);
    }

    protected FluxRecordIterator queryIterator(@Nonnull final Call<ResponseBody> queryCall) {
        return new FluxRecordIterator(queryCall, ERROR_CONSUMER);
    }

    protected void queryRaw(@Nonnull final Call<ResponseBody> queryCall,
                            @Nonnull final BiConsumer<Cancellable, String> onResponse,
                            @Nonnull final Consumer<? super Throwable> onError,
                            @Nonnull final Runnable onComplete,
                            @Nonnull final Boolean asynchronously) {

        BiConsumer<Cancellable, BufferedSource> consumer = (cancellable, bufferedSource) -> {

            try {
                parseFluxResponseToLines(line -> onResponse.accept(cancellable, line), cancellable, bufferedSource);
            } catch (IOException e) {
                catchOrPropagateException(e, onError);
            }
        };

        query(queryCall, consumer, onError, onComplete, asynchronously);
    }

    protected RawIterator queryRawIterator(@Nonnull final Call<ResponseBody> queryCall) {
        return new RawIterator(queryCall, ERROR_CONSUMER);
    }

    private void query(@Nonnull final Call<ResponseBody> query,
                       @Nonnull final BiConsumer<Cancellable, BufferedSource> consumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNotNull(consumer, "consumer");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(asynchronously, "asynchronously");

        DefaultCancellable cancellable = new DefaultCancellable();

        Consumer<ResponseBody> bodyConsumer = body -> {
            try {
                BufferedSource source = body.source();

                //
                // Source has data => parse
                //
                while (source.isOpen() && !source.exhausted() && !cancellable.wasCancelled) {

                    consumer.accept(cancellable, source);
                }

                if (!cancellable.wasCancelled) {
                    onComplete.run();
                }

            } catch (Exception e) {
                catchOrPropagateException(e, onError);

            } finally {

                body.close();
            }
        };

        query(query, bodyConsumer, onError, onComplete, asynchronously);
    }

    private void query(@Nonnull final Call<ResponseBody> query,
                       @Nonnull final Consumer<ResponseBody> consumer,
                       @Nonnull final Consumer<? super Throwable> onError,
                       @Nonnull final Runnable onComplete,
                       @Nonnull final Boolean asynchronously) {

        Arguments.checkNotNull(query, "query");
        Arguments.checkNotNull(consumer, "consumer");
        Arguments.checkNotNull(onError, "onError");
        Arguments.checkNotNull(onComplete, "onComplete");
        Arguments.checkNotNull(asynchronously, "asynchronously");

        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(@Nonnull final Call<ResponseBody> call,
                                   @Nonnull final Response<ResponseBody> response) {

                if (!response.isSuccessful()) {
                    onError.accept(responseToError(response));
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    return;
                }

                consumer.accept(body);
            }

            @Override
            public void onFailure(@Nonnull final Call<ResponseBody> call, @Nonnull final Throwable throwable) {
                onError.accept(throwable);
            }
        };

        LOG.log(Level.FINEST, "Prepared query {0}, asynchronously {1}", new Object[]{query, asynchronously});

        if (asynchronously) {
            query.enqueue(callback);
        } else {

            Response<ResponseBody> response;
            try {
                response = query.execute();
                callback.onResponse(query, response);
            } catch (IOException e) {
                catchOrPropagateException(e, onError);
            }
        }
    }

    private void parseFluxResponseToLines(@Nonnull final Consumer<String> onResponse,
                                          @Nonnull final Cancellable cancellable,
                                          @Nonnull final BufferedSource bufferedSource) throws IOException {

        String line = bufferedSource.readUtf8Line();

        while (line != null && !cancellable.isCancelled()) {
            onResponse.accept(line);
            line = bufferedSource.readUtf8Line();
        }
    }

    private class DefaultCancellable implements Cancellable {

        private volatile boolean wasCancelled = false;

        @Override
        public void cancel() {
            wasCancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return wasCancelled;
        }
    }

    protected final class RawIterator implements Iterator<String>, Closeable, Consumer<ResponseBody> {

        private String line = null;
        private boolean closed = false;
        private ResponseBody body;
        private BufferedSource source;
        private final Consumer<? super Throwable> onError;

        private RawIterator(@Nonnull final Call<ResponseBody> call,
                            @Nonnull final Consumer<? super Throwable> onError) {
            this.onError = onError;
            query(call, this, onError, EMPTY_ACTION, false);
        }

        @Override
        public boolean hasNext() {
            return !closed && readNext();
        }

        @Override
        public String next() {
            return line;
        }

        @Override
        public void accept(final ResponseBody body) {
            this.body = body;
            this.source = body.source();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (body != null) {
                body.close();
            }
        }

        private boolean readNext() {
            line = null;
            try {
                if (!closed && source.isOpen() && !source.exhausted()) {
                    line = source.readUtf8Line();
                }
            } catch (IOException e) {
                catchOrPropagateException(e, onError);
            }

            return line != null;
        }
    }

    protected final class FluxRecordIterator implements Iterator<FluxRecord>, Closeable, Consumer<ResponseBody> {

        private FluxRecord record = null;
        private boolean closed = false;
        private ResponseBody body;
        private CSVParser parser;
        private Iterator<CSVRecord> iterator;

        private final FluxCsvParser.FluxCsvState state = new FluxCsvParser.FluxCsvState();
        private final Consumer<? super Throwable> onError;

        public FluxRecordIterator(@Nonnull final Call<ResponseBody> call,
                                  @Nonnull final Consumer<? super Throwable> onError) {
            this.onError = onError;
            query(call, this, onError, EMPTY_ACTION, false);
        }

        @Override
        public boolean hasNext() {
            return !closed && readNext();
        }

        @Override
        public FluxRecord next() {
            return record;
        }

        @Override
        public void accept(final ResponseBody body) {
            this.body = body;

            Reader reader = new InputStreamReader(body.source().inputStream(), StandardCharsets.UTF_8);
            try {
                parser = new CSVParser(reader, CSVFormat.DEFAULT);
            } catch (IOException e) {
                catchOrPropagateException(e, onError);
            }
            iterator = parser.iterator();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (parser != null) {
                parser.close();
            }
            if (body != null) {
                body.close();
            }
        }

        private boolean readNext() {

            record = null;
            while (record == null && iterator.hasNext()) {
                state.csvRecord = iterator.next();
                FluxCsvParser.FluxRecordOrTable fluxRecordOrTable = fluxCsvParser.parseNextResponse(state);
                if (fluxRecordOrTable.record != null) {
                    record = fluxRecordOrTable.record;
                }
            }

            return record != null;
        }
    }
}