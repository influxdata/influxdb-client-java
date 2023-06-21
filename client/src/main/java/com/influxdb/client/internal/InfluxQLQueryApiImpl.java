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
package com.influxdb.client.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Cancellable;
import com.influxdb.client.InfluxQLQueryApi;
import com.influxdb.client.domain.InfluxQLQuery;
import com.influxdb.client.service.InfluxQLQueryService;
import com.influxdb.internal.AbstractQueryApi;
import com.influxdb.query.InfluxQLQueryResult;
import com.influxdb.utils.Arguments;

import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import retrofit2.Call;

public class InfluxQLQueryApiImpl extends AbstractQueryApi implements InfluxQLQueryApi {

    private final InfluxQLQueryService service;

    public InfluxQLQueryApiImpl(@Nonnull final InfluxQLQueryService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Nonnull
    @Override
    public InfluxQLQueryResult query(@Nonnull final InfluxQLQuery influxQlQuery) {
        return query(influxQlQuery, null);
    }

    @Nonnull
    @Override
    public InfluxQLQueryResult query(
            @Nonnull final InfluxQLQuery influxQlQuery,
            @Nullable final InfluxQLQueryResult.Series.ValueExtractor valueExtractor
    ) {
        Call<ResponseBody> call = service.query(
                influxQlQuery.getCommand(),
                influxQlQuery.getDatabase(),
                influxQlQuery.getRetentionPolicy(),
                influxQlQuery.getPrecision() != null ? influxQlQuery.getPrecision().getSymbol() : null,
                null);

        AtomicReference<InfluxQLQueryResult> atomicReference = new AtomicReference<>();
        BiConsumer<Cancellable, BufferedSource> consumer = (cancellable, bufferedSource) -> {
            try {
                InfluxQLQueryResult result = parseResponse(bufferedSource, cancellable, valueExtractor);
                atomicReference.set(result);
            } catch (IOException e) {
                ERROR_CONSUMER.accept(e);
            }
        };
        query(call, consumer, ERROR_CONSUMER, EMPTY_ACTION, false);
        return atomicReference.get();
    }

    private InfluxQLQueryResult parseResponse(
            @Nonnull final BufferedSource bufferedSource,
            @Nonnull final Cancellable cancellable,
            @Nullable final InfluxQLQueryResult.Series.ValueExtractor valueExtractor) throws IOException {

        Arguments.checkNotNull(bufferedSource, "bufferedSource");

        try (Reader reader = new InputStreamReader(bufferedSource.inputStream(), StandardCharsets.UTF_8)) {
            return readInfluxQLResult(reader, cancellable, valueExtractor);
        }
    }

    static InfluxQLQueryResult readInfluxQLResult(
            @Nonnull final Reader reader,
            @Nonnull final Cancellable cancellable,
            @Nullable final InfluxQLQueryResult.Series.ValueExtractor valueExtractor
    ) throws IOException {
        List<InfluxQLQueryResult.Result> results = new ArrayList<>();
        Map<List<Object>, InfluxQLQueryResult.Series> series = null;
        Map<String, Integer> headerCols = null;
        final int nameCol = 0;
        final int tagsCol = 1;
        // The first 2 columns are static (`name`, `tags`) and got skipped.
        // All other columns are dynamically parsed
        final int dynamicColumnsStartIndex = 2;

        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setIgnoreEmptyLines(false).build())) {
            for (CSVRecord csvRecord : parser) {
                if (cancellable.isCancelled()) {
                    break;
                }
                int resultIndex = results.size();
                if (csvRecord.size() == 1 || csvRecord.get(0).equals("")) {
                    if (series != null) {
                        InfluxQLQueryResult.Result result = new InfluxQLQueryResult.Result(
                                resultIndex,
                                new ArrayList<>(series.values())
                        );
                        results.add(result);
                    }
                    series = null;
                    continue;
                }

                if (series == null) {

                    List<String> header = csvRecord.toList();
                    headerCols = new LinkedHashMap<>();
                    for (int col = dynamicColumnsStartIndex; col < header.size(); col++) {
                        String colName = header.get(col);
                        headerCols.put(colName, col - dynamicColumnsStartIndex);
                    }
                    series = new LinkedHashMap<>();

                } else {
                    String name = csvRecord.get(nameCol);
                    Map<String, String> finalTags = parseTags(csvRecord.get(tagsCol));
                    Map<String, Integer> finalHeaderCols = headerCols;
                    InfluxQLQueryResult.Series serie = series.computeIfAbsent(
                            Arrays.asList(name, finalTags),
                            n -> new InfluxQLQueryResult.Series(name, finalTags, finalHeaderCols)
                    );
                    Object[] values = headerCols.entrySet().stream().map(entry -> {
                        String value = csvRecord.get(entry.getValue() + dynamicColumnsStartIndex);
                        if (valueExtractor != null) {
                            return valueExtractor.extractValue(entry.getKey(), value, resultIndex, serie.getName());
                        }
                        return value;
                    }).toArray();
                    InfluxQLQueryResult.Series.Record record = serie.new Record(values);
                    serie.addRecord(record);
                }
            }
        }
        if (series != null) {
            InfluxQLQueryResult.Result result = new InfluxQLQueryResult.Result(
                    results.size(),
                    new ArrayList<>(series.values())
            );
            results.add(result);
        }
        return new InfluxQLQueryResult(results);
    }

    private static Map<String, String> parseTags(@Nonnull final String value) {
        final Map<String, String> tags = new HashMap<>();
        if (value.length() > 0) {
            for (String entry : value.split(",")) {
                final String[] kv = entry.split("=");
                tags.put(kv[0], kv[1]);
            }
        }

        return tags;
    }
}
