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
package com.influxdb.client.osgi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LineProtocolWriterTest {

    private LineProtocolWriter lineProtocolWriter;

    private static final String EVENT_TOPIC = "influxdb/test";
    private static final String RECORD1_WITHOUT_TIMESTAMP = "weather,location=HU temperature=5.89,humidity=48";
    private static final String RECORD2_WITHOUT_TIMESTAMP = "weather,location=HU temperature=5.93,humidity=48";
    private static final String RECORD3_WITHOUT_TIMESTAMP = "weather,location=HU temperature=5.90,humidity=52";
    private static final String RECORD4_WITHOUT_TIMESTAMP = "weather,location=HU temperature=6.03,humidity=53";
    private static final String RECORD5_WITHOUT_TIMESTAMP = "weather,location=HU temperature=5.98,humidity=47";
    private static final String ORGANIZATION_OVERRIDE = "my-org";
    private static final String BUCKET_OVERRIDE = "my-bucket";

    @Mock
    private WriteApiBlocking writeApi;

    @Mock
    private InfluxDBClient client;

    @Mock
    private LineProtocolWriter.Config config;

    @Captor
    private ArgumentCaptor<WritePrecision> precisionCaptor;

    @Captor
    private ArgumentCaptor<String> recordCaptor;

    @Captor
    private ArgumentCaptor<List<String>> recordsCaptor;

    @Captor
    private ArgumentCaptor<String> organizationCaptor;

    @Captor
    private ArgumentCaptor<String> bucketCaptor;

    private Map<String, Object> payload;

    @BeforeEach
    void setUp() {
        lineProtocolWriter = new LineProtocolWriter();
        when(client.getWriteApiBlocking()).thenReturn(writeApi);
        lineProtocolWriter.client = client;
        payload = new TreeMap<>();
        payload.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
    }

    void testLineProtocolWriter() {
        lineProtocolWriter.start(config);
        final Event event = new Event(EVENT_TOPIC, payload);
        lineProtocolWriter.handleEvent(event);
    }

    @Test
    void testSingleRecordDefaultPrecision() {
        final String record = RECORD1_WITHOUT_TIMESTAMP + " 1617826119000000000";
        payload.put(LineProtocolWriter.RECORD, record);

        testLineProtocolWriter();

        payload.put(LineProtocolWriter.ORGANIZATION, ORGANIZATION_OVERRIDE);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        payload.remove(LineProtocolWriter.ORGANIZATION);
        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        verify(writeApi).writeRecord(bucketCaptor.capture(), organizationCaptor.capture(), precisionCaptor.capture(), recordCaptor.capture());
        verify(writeApi, times(3)).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        precisionCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(WritePrecision.NS)));
        recordCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(record)));
        organizationCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(ORGANIZATION_OVERRIDE)));
        bucketCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(BUCKET_OVERRIDE)));
    }

    @Test
    void testNoRecord() {
        assertThrows(IllegalArgumentException.class, () -> testLineProtocolWriter());
    }

    @Test
    void testRecordSetDefaultPrecision() {
        final String record1 = RECORD1_WITHOUT_TIMESTAMP + " 1617826119000000000";
        final String record2 = RECORD2_WITHOUT_TIMESTAMP + " 1617826120000000000";
        final String record3 = RECORD3_WITHOUT_TIMESTAMP + " 1617826121000000000";
        final String record4 = RECORD4_WITHOUT_TIMESTAMP + " 1617826122000000000";
        final String record5 = RECORD5_WITHOUT_TIMESTAMP + " 1617826123000000000";

        final List<String> records = Arrays.asList(record1, record2, record3, record4, record5);
        payload.put(LineProtocolWriter.RECORDS, records);

        testLineProtocolWriter();

        payload.put(LineProtocolWriter.ORGANIZATION, ORGANIZATION_OVERRIDE);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        payload.remove(LineProtocolWriter.ORGANIZATION);
        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        verify(writeApi).writeRecords(bucketCaptor.capture(), organizationCaptor.capture(), precisionCaptor.capture(), recordsCaptor.capture());
        verify(writeApi, times(3)).writeRecords(precisionCaptor.capture(), recordsCaptor.capture());
        precisionCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(WritePrecision.NS)));
        recordsCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(records)));
        organizationCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(ORGANIZATION_OVERRIDE)));
        bucketCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(BUCKET_OVERRIDE)));
    }

    @Test
    void testRecordSetAddedTimestamp() {
        when(config.timestamp_append()).thenReturn(true);

        final String record1 = RECORD1_WITHOUT_TIMESTAMP;
        final String record2 = RECORD2_WITHOUT_TIMESTAMP;
        final String record3 = RECORD3_WITHOUT_TIMESTAMP;
        final String record4 = RECORD4_WITHOUT_TIMESTAMP;
        final String record5 = RECORD5_WITHOUT_TIMESTAMP;

        final List<String> records = Arrays.asList(record1, record2, record3, record4, record5);
        payload.put(LineProtocolWriter.RECORDS, records);

        testLineProtocolWriter();

        payload.put(LineProtocolWriter.ORGANIZATION, ORGANIZATION_OVERRIDE);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        payload.remove(LineProtocolWriter.ORGANIZATION);
        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testLineProtocolWriter();

        verify(writeApi).writeRecords(bucketCaptor.capture(), organizationCaptor.capture(), precisionCaptor.capture(), recordsCaptor.capture());
        verify(writeApi, times(3)).writeRecords(precisionCaptor.capture(), recordsCaptor.capture());

        final List<String> expectedRecords = records.stream()
                .map(r -> r + " " + (Long) payload.get(EventConstants.TIMESTAMP) * 1000000)
                .collect(Collectors.toList());
        precisionCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(WritePrecision.NS)));
        recordsCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(expectedRecords)));
        organizationCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(ORGANIZATION_OVERRIDE)));
        bucketCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(BUCKET_OVERRIDE)));
    }

    @Test
    void testSingleRecordWithPrecision() {
        final String record = RECORD1_WITHOUT_TIMESTAMP + " 1617826119";
        payload.put(LineProtocolWriter.RECORD, record);
        payload.put(LineProtocolWriter.PRECISION, "s");

        testLineProtocolWriter();

        verify(writeApi).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        assertThat(precisionCaptor.getValue(), equalTo(WritePrecision.S));
        assertThat(recordCaptor.getValue(), equalTo(record));
    }

    @Test
    void testSingleRecordAddedTimestampDefaultPrecision() {
        when(config.timestamp_append()).thenReturn(true);

        final String record = RECORD1_WITHOUT_TIMESTAMP;
        payload.put(LineProtocolWriter.RECORD, record);

        testLineProtocolWriter();

        verify(writeApi).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        final String expectedRecord = record + " " + (Long)payload.get(EventConstants.TIMESTAMP) * 1000000L;
        assertThat(precisionCaptor.getValue(), equalTo(WritePrecision.NS));
        assertThat(recordCaptor.getValue(), equalTo(expectedRecord));
    }

    @Test
    void testSingleRecordAddedTimestampWithNanoSecondPrecision() {
        when(config.timestamp_append()).thenReturn(true);

        final String record = RECORD1_WITHOUT_TIMESTAMP;
        payload.put(LineProtocolWriter.RECORD, record);
        payload.put(LineProtocolWriter.PRECISION, "ns");

        testLineProtocolWriter();

        verify(writeApi).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        final String expectedRecord = record + " " + payload.get(EventConstants.TIMESTAMP) + "000000";
        assertThat(precisionCaptor.getValue(), equalTo(WritePrecision.NS));
        assertThat(recordCaptor.getValue(), equalTo(expectedRecord));
    }

    @Test
    void testSingleRecordAddedTimestampWithPrecision() {
        when(config.timestamp_append()).thenReturn(true);

        final String record = RECORD1_WITHOUT_TIMESTAMP;
        payload.put(LineProtocolWriter.RECORD, record);

        payload.put(LineProtocolWriter.PRECISION, "s");
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, WritePrecision.MS);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, "us");
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, WritePrecision.NS);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, "x");
        assertThrows(IllegalArgumentException.class, () -> testLineProtocolWriter());

        verify(writeApi, times(4)).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        final List<String> expectedRecords = Arrays.asList(
                record + " " + ((Long) payload.get(EventConstants.TIMESTAMP)) / 1000,
                record + " " + payload.get(EventConstants.TIMESTAMP),
                record + " " + ((Long) payload.get(EventConstants.TIMESTAMP)) * 1000,
                record + " " + ((Long) payload.get(EventConstants.TIMESTAMP)) * 1000000
        );
        final List<WritePrecision> expectedPrecisions = Arrays.asList(
                WritePrecision.S, WritePrecision.MS, WritePrecision.US, WritePrecision.NS
        );
        assertThat(precisionCaptor.getAllValues(), equalTo(expectedPrecisions));
        assertThat(recordCaptor.getAllValues(), equalTo(expectedRecords));
    }

    @Test
    void testSingleRecordAddedSystemTimestamp() {
        when(config.timestamp_append()).thenReturn(true);
        payload.remove(EventConstants.TIMESTAMP);

        final String record = RECORD1_WITHOUT_TIMESTAMP;
        payload.put(LineProtocolWriter.RECORD, record);

        final long startTs = System.currentTimeMillis();

        payload.put(LineProtocolWriter.PRECISION, WritePrecision.S);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, "ms");
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, WritePrecision.US);
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, "ns");
        testLineProtocolWriter();

        payload.put(LineProtocolWriter.PRECISION, "x");
        assertThrows(IllegalArgumentException.class, () -> testLineProtocolWriter());

        final long endTs = System.currentTimeMillis() + 1;

        verify(writeApi, times(4)).writeRecord(precisionCaptor.capture(), recordCaptor.capture());
        final List<Long> startTsList = Arrays.asList(
                startTs / 1000, startTs, startTs * 1000, startTs * 1000000
        );
        final List<Long> endTsList = Arrays.asList(
                endTs / 1000, endTs, endTs * 1000, endTs * 1000000
        );
        final List<WritePrecision> expectedPrecisions = Arrays.asList(
                WritePrecision.S, WritePrecision.MS, WritePrecision.US, WritePrecision.NS
        );
        assertThat(precisionCaptor.getAllValues(), equalTo(expectedPrecisions));
        assertThat(recordCaptor.getAllValues(), hasSize(4));
        recordCaptor.getAllValues().forEach(v -> assertThat(v, startsWith(record)));
        IntStream.rangeClosed(0, 3).forEach(i -> {
            final Long timestamp = Long.parseLong(recordCaptor.getAllValues().get(i).replace(record + " ", ""));
            assertThat(timestamp, allOf(greaterThanOrEqualTo(startTsList.get(i)), lessThanOrEqualTo(endTsList.get(i))));
        });
    }
}
