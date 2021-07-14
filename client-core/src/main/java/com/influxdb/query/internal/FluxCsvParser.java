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
package com.influxdb.query.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.influxdb.Arguments;
import com.influxdb.Cancellable;
import com.influxdb.query.FluxColumn;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.exceptions.FluxCsvParserException;
import com.influxdb.query.exceptions.FluxQueryException;

import okio.BufferedSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class us used to construct FluxResult from CSV.
 *
 * @see com.influxdb.query
 */
public class FluxCsvParser {

    private static final String ANNOTATION_DATATYPE = "#datatype";
    private static final String ANNOTATION_GROUP = "#group";
    private static final String ANNOTATION_DEFAULT = "#default";
    private static final List<String> ANNOTATIONS = Arrays
            .asList(ANNOTATION_DATATYPE, ANNOTATION_GROUP, ANNOTATION_DEFAULT);

    private enum ParsingState {
        NORMAL,

        IN_ERROR
    }

    public interface FluxResponseConsumer {

        /**
         * Add new {@link FluxTable} to consumer.
         *
         * @param index       index of table
         * @param cancellable cancellable
         * @param table       new {@link FluxTable}
         */
        void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxTable table);

        /**
         * Add new {@link FluxRecord} to consumer.
         *
         * @param index       index of table
         * @param cancellable cancellable
         * @param record      new {@link FluxRecord}
         */

        void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxRecord record);
    }

    public class FluxResponseConsumerTable implements FluxCsvParser.FluxResponseConsumer {

        private List<FluxTable> tables = new ArrayList<>();

        @Override
        public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxTable table) {
            tables.add(index, table);
        }

        @Override
        public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxRecord record) {
            tables.get(index).getRecords().add(record);
        }

        @Nonnull
        public List<FluxTable> getTables() {
            return tables;
        }
    }

    /**
     * Parse Flux CSV response to {@link FluxResponseConsumer}.
     *
     * @param bufferedSource with data
     * @param cancellable    to cancel parsing
     * @param consumer       to accept {@link FluxTable}s and {@link FluxRecord}s
     * @throws IOException If there is a problem with reading CSV
     */
    @SuppressWarnings("MagicNumber")
    public void parseFluxResponse(@Nonnull final BufferedSource bufferedSource,
                                  @Nonnull final Cancellable cancellable,
                                  @Nonnull final FluxResponseConsumer consumer) throws IOException {

        Arguments.checkNotNull(bufferedSource, "bufferedSource");

        Reader reader = new InputStreamReader(bufferedSource.inputStream(), StandardCharsets.UTF_8);

        FluxCsvState state = new FluxCsvState();

        try (CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            for (CSVRecord csvRecord : parser) {

                if (cancellable.isCancelled()) {
                    return;
                }

                state.csvRecord = csvRecord;

                FluxRecordOrTable fluxRecordOrTable = parseNextResponse(state);
                if (fluxRecordOrTable.table != null) {
                    consumer.accept(state.tableIndex - 1, cancellable, fluxRecordOrTable.table);
                }
                if (fluxRecordOrTable.record != null) {
                    consumer.accept(state.tableIndex - 1, cancellable, fluxRecordOrTable.record);
                }
            }
        }
    }

    /**
     * Parse actual {@link FluxCsvState#csvRecord} into {@link FluxTable} or {@link FluxRecord}.
     *
     * @param state current state of parsing
     * @return table or record or both
     */
    @Nonnull
    @SuppressWarnings("MagicNumber")
    public FluxRecordOrTable parseNextResponse(@Nonnull final FluxCsvState state) {
        FluxRecordOrTable result = new FluxRecordOrTable();
        CSVRecord csvRecord = state.csvRecord;

        //
        // Response has HTTP status ok, but response is error.
        //
        if (csvRecord.size() >= 3 && csvRecord.get(1).equals("error") && csvRecord.get(2).equals("reference")) {

            state.parsingState = ParsingState.IN_ERROR;
            return result;
        }

        //
        // Throw InfluxException with error response
        //
        if (ParsingState.IN_ERROR.equals(state.parsingState)) {
            String error = csvRecord.get(1);
            String referenceValue = csvRecord.get(2);

            int reference = 0;
            if (referenceValue != null && !referenceValue.isEmpty()) {
                reference = Integer.parseInt(referenceValue);
            }

            throw new FluxQueryException(error, reference);
        }

        String token = csvRecord.get(0);
        //// start new table
        if (ANNOTATIONS.contains(token) && !state.startNewTable) {
            state.startNewTable = true;

            state.table = new FluxTable();
            state.groups = Collections.emptyList();
            result.table = state.table;
            state.tableIndex++;
            state.tableId = -1;

        } else if (state.table == null) {
            String message = "Unable to parse CSV response. FluxTable definition was not found.";
            throw new FluxCsvParserException(message);
        }

        //#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string
        if (ANNOTATION_DATATYPE.equals(token)) {
            addDataTypes(state.table, toList(csvRecord));

        } else if (ANNOTATION_GROUP.equals(token)) {
            state.groups = toList(csvRecord);
        } else if (ANNOTATION_DEFAULT.equals(token)) {
            addDefaultEmptyValues(state.table, toList(csvRecord));
        } else {
            // parse column names
            if (state.startNewTable) {
                addGroups(state.table, state.groups);
                addColumnNamesAndTags(state.table, toList(csvRecord));
                state.startNewTable = false;
                return result;
            }

            int currentId = Integer.parseInt(csvRecord.get(1 + 1));
            if (state.tableId == -1) {
                state.tableId = currentId;
            }

            if (state.tableId != currentId) {
                //create new table with previous column headers settings
                List<FluxColumn> fluxColumns = state.table.getColumns();
                state.table = new FluxTable();
                state.table.getColumns().addAll(fluxColumns);
                result.table = state.table;
                state.tableIndex++;
                state.tableId = currentId;
            }

            result.record = parseRecord(state.tableIndex - 1, state.table, csvRecord);
        }

        return result;
    }

    private FluxRecord parseRecord(final int tableIndex, final FluxTable table, final CSVRecord csvRecord) {

        FluxRecord record = new FluxRecord(tableIndex);

        for (FluxColumn fluxColumn : table.getColumns()) {

            String columnName = fluxColumn.getLabel();

            String strValue = csvRecord.get(fluxColumn.getIndex() + 1);

            record.getValues().put(columnName, toValue(strValue, fluxColumn));
        }
        return record;
    }

    @Nonnull
    private List<String> toList(final CSVRecord csvRecord) {
        List<String> ret = new ArrayList<>(csvRecord.size());
        int size = csvRecord.size();

        for (int i = 1; i < size; i++) {
            String rec = csvRecord.get(i);
            ret.add(rec);
        }
        return ret;
    }

    @Nullable
    private Object toValue(@Nullable final String strValue, final @Nonnull FluxColumn column) {

        Arguments.checkNotNull(column, "column");

        // Default value
        if (strValue == null || strValue.isEmpty()) {
            String defaultValue = column.getDefaultValue();
            if (defaultValue == null || defaultValue.isEmpty()) {
                return null;
            }

            return toValue(defaultValue, column);
        }

        String dataType = column.getDataType();
        switch (dataType) {
            case "boolean":
                return Boolean.valueOf(strValue);
            case "unsignedLong":
                return Long.parseUnsignedLong(strValue);
            case "long":
                return Long.parseLong(strValue);
            case "double":
                switch (strValue) {
                    case "+Inf":
                        return Double.POSITIVE_INFINITY;
                    case "-Inf":
                        return Double.NEGATIVE_INFINITY;
                    default:
                        return Double.parseDouble(strValue);
                }
            case "base64Binary":
                return Base64.getDecoder().decode(strValue);
            case "dateTime:RFC3339":
            case "dateTime:RFC3339Nano":
                return Instant.parse(strValue);
            case "duration":
                return Duration.ofNanos(Long.parseUnsignedLong(strValue));
            default:
            case "string":
                return strValue;
        }
    }

    private void addDataTypes(@Nonnull final FluxTable table,
                              @Nonnull final List<String> dataTypes) {

        Arguments.checkNotNull(table, "table");
        Arguments.checkNotNull(dataTypes, "dataTypes");

        for (int index = 0; index < dataTypes.size(); index++) {
            String dataType = dataTypes.get(index);

            FluxColumn columnDef = new FluxColumn();
            columnDef.setDataType(dataType);
            columnDef.setIndex(index);

            table.getColumns().add(columnDef);
        }
    }

    private void addGroups(@Nonnull final FluxTable table, @Nonnull final List<String> groups) {

        Arguments.checkNotNull(table, "table");
        Arguments.checkNotNull(groups, "groups");

        for (int i = 0; i < table.getColumns().size(); i++) {

            FluxColumn fluxColumn = getFluxColumn(i, table);

            String group = groups.get(i);
            fluxColumn.setGroup(Boolean.parseBoolean(group));
        }
    }

    private void addDefaultEmptyValues(@Nonnull final FluxTable table, @Nonnull final List<String> defaultEmptyValues) {

        Arguments.checkNotNull(table, "table");
        Arguments.checkNotNull(defaultEmptyValues, "defaultEmptyValues");

        for (int i = 0; i < defaultEmptyValues.size(); i++) {

            FluxColumn fluxColumn = getFluxColumn(i, table);

            String defaultValue = defaultEmptyValues.get(i);
            fluxColumn.setDefaultValue(defaultValue);
        }

    }

    private void addColumnNamesAndTags(@Nonnull final FluxTable table, @Nonnull final List<String> columnNames) {

        Arguments.checkNotNull(table, "table");
        Arguments.checkNotNull(columnNames, "columnNames");

        int size = columnNames.size();

        for (int i = 0; i < size; i++) {

            FluxColumn fluxColumn = getFluxColumn(i, table);

            String columnName = columnNames.get(i);
            fluxColumn.setLabel(columnName);
        }
    }

    @Nonnull
    private FluxColumn getFluxColumn(final int columnIndex, final @Nonnull FluxTable table) {

        Arguments.checkNotNull(table, "table");

        return table.getColumns().get(columnIndex);
    }

    public static class FluxCsvState {
        private ParsingState parsingState = ParsingState.NORMAL;

        private int tableIndex = 0;
        private int tableId = -1;
        private boolean startNewTable = false;
        private FluxTable table = null;
        private List<String> groups = Collections.emptyList();
        public CSVRecord csvRecord;
    }

    public static class FluxRecordOrTable {
        public FluxRecord record;
        public FluxTable table;
    }
}
