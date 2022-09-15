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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointWriterTest {

    private PointWriter pointWriter;

    private static final String EVENT_TOPIC = "influxdb/weather";
    private static final Supplier<Point> POINT1_WITHOUT_TIMESTAMP = () -> Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.89)
            .addField("humidity", 48);
    private static final Supplier<Point> POINT2_WITHOUT_TIMESTAMP = () -> Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.93)
            .addField("humidity", 48);
    private static final Supplier<Point> POINT3_WITHOUT_TIMESTAMP = () ->Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.90)
            .addField("humidity", 52);
    private static final Supplier<Point> POINT4_WITHOUT_TIMESTAMP = () -> Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 6.03)
            .addField("humidity", 53);
    private static final Supplier<Point> POINT5_WITHOUT_TIMESTAMP = () -> Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.98)
            .addField("humidity", 47);
    private static final Supplier<Map<String, Object>> MAP1 = () -> {
        final Map<String, Object> data = new TreeMap<>();
        final Map<String, String> tags = new TreeMap<>();
        tags.put("location", "HU");
        final Map<String, Object> fields = new TreeMap<>();
        fields.put("temperature", 5.89);
        fields.put("humidity", 48);
        data.put(PointWriter.TAGS_KEY, tags);
        data.put(PointWriter.FIELDS_KEY, fields);
        return data;
    };

    private static final String ORGANIZATION_OVERRIDE = "my-org";
    private static final String BUCKET_OVERRIDE = "my-bucket";

    @Mock
    private WriteApiBlocking writeApi;

    @Mock
    private InfluxDBClient client;

    @Mock
    private PointWriter.Config config;

    @Captor
    private ArgumentCaptor<Point> pointCaptor;

    @Captor
    private ArgumentCaptor<List<Point>> pointsCaptor;

    @Captor
    private ArgumentCaptor<String> organizationCaptor;

    @Captor
    private ArgumentCaptor<String> bucketCaptor;

    private Map<String, Object> payload;

    @BeforeEach
    void setUp() {
        pointWriter = new PointWriter();
        when(client.getWriteApiBlocking()).thenReturn(writeApi);
        pointWriter.client = client;
        payload = new TreeMap<>();
        payload.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
    }

    void testPointWriter() {
        pointWriter.start(config);
        final Event event = new Event(EVENT_TOPIC, payload);
        pointWriter.handleEvent(event);
    }

    @Test
    void testSinglePointDefaultPrecision() {
        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        payload.put(LineProtocolWriter.ORGANIZATION, ORGANIZATION_OVERRIDE);
        testPointWriter();

        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testPointWriter();

        payload.remove(LineProtocolWriter.ORGANIZATION);
        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testPointWriter();

        verify(writeApi).writePoint(bucketCaptor.capture(), organizationCaptor.capture(), pointCaptor.capture());
        verify(writeApi, times(3)).writePoint(pointCaptor.capture());
        pointCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(point)));
        organizationCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(ORGANIZATION_OVERRIDE)));
        bucketCaptor.getAllValues().forEach(v -> assertThat(v, equalTo(BUCKET_OVERRIDE)));
    }

    @Test
    void testSingleMapDefaultPrecision() {
        when(config.timestamp_precision()).thenReturn("ms");

        final Map<String, Object> map = new TreeMap<>(MAP1.get());
        ((Map<String, Object>) map.get(PointWriter.FIELDS_KEY)).put("raining", false);
        final LocalDate date = LocalDate.now();
        ((Map<String, Object>) map.get(PointWriter.FIELDS_KEY)).put("date", date);
        payload.put(PointWriter.POINT, map);

        final long startTs = (Long) payload.get(EventConstants.TIMESTAMP);

        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, System.currentTimeMillis());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, new Date());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, LocalDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, OffsetDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, ZonedDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_KEY, Instant.now());
        testPointWriter();

        map.remove(PointWriter.TIMESTAMP_KEY);
        payload.remove(EventConstants.TIMESTAMP);
        testPointWriter();

        final long endTs = System.currentTimeMillis();

        verify(writeApi, times(8)).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getAllValues(), hasSize(8));

        final Point point = POINT1_WITHOUT_TIMESTAMP.get()
                .addField("raining", false)
                .addFields(Collections.singletonMap("date", date));
        final String originalWithoutTimestamp = point.toLineProtocol().replaceFirst(" [0-9]+$", "");

        IntStream.rangeClosed(0, 7).forEach(i -> {
            final String written = pointCaptor.getAllValues().get(i).toLineProtocol();
            final Long timestamp = Long.parseLong(written.replace(originalWithoutTimestamp + " ", ""));
            assertThat(timestamp, allOf(greaterThanOrEqualTo(startTs), lessThanOrEqualTo(endTs)));
        });
    }

    @Test
    void testSingleMapOsgiEventTimestamp() {
        when(config.timestamp_add()).thenReturn(true);
        when(config.timestamp_precision()).thenReturn("ms");
        when(config.host_name_add()).thenReturn(false);
        when(config.host_address_add()).thenReturn(false);

        final Map<String, Object> map = new TreeMap<>(MAP1.get());
        payload.put(PointWriter.POINTS, Arrays.asList(map));

        testPointWriter();

        verify(writeApi).writePoints(pointsCaptor.capture());
        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time((Long) payload.get(EventConstants.TIMESTAMP), WritePrecision.MS);
        assertThat(pointsCaptor.getValue(), hasSize(1));
        assertThat(pointsCaptor.getValue().get(0).toLineProtocol(), equalTo(point.toLineProtocol()));
    }

    @Test
    void testNoPoint() {
        assertThrows(IllegalArgumentException.class, () -> testPointWriter());
    }

    @Test
    void testInvalidPoint() {
        payload.put(PointWriter.POINT, "weather,location=HU temperature=5.89,humidity=48 1617826119000");
        assertThrows(IllegalArgumentException.class, () -> testPointWriter());
    }

    @Test
    void testRecordSetDefaultPrecision() {
        final Point point1 = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        final Point point2 = POINT2_WITHOUT_TIMESTAMP.get().time(1617826120000L, WritePrecision.MS);
        final Point point3 = POINT3_WITHOUT_TIMESTAMP.get().time(1617826121000L, WritePrecision.MS);
        final Point point4 = POINT4_WITHOUT_TIMESTAMP.get().time(1617826122000L, WritePrecision.MS);
        final Point point5 = POINT5_WITHOUT_TIMESTAMP.get().time(1617826123000L, WritePrecision.MS);

        final List<Point> points = Arrays.asList(point1, point2, point3, point4, point5);
        payload.put(PointWriter.POINTS, points);

        testPointWriter();

        payload.put(LineProtocolWriter.ORGANIZATION, ORGANIZATION_OVERRIDE);
        testPointWriter();

        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testPointWriter();

        payload.remove(LineProtocolWriter.ORGANIZATION);
        payload.put(LineProtocolWriter.BUCKET, BUCKET_OVERRIDE);
        testPointWriter();

        verify(writeApi).writePoints(bucketCaptor.capture(), organizationCaptor.capture(), pointsCaptor.capture());
        verify(writeApi, times(3)).writePoints(pointsCaptor.capture());
        pointsCaptor.getAllValues().forEach(v -> assertThat(v, CoreMatchers.equalTo(points)));
        organizationCaptor.getAllValues().forEach(v -> assertThat(v, CoreMatchers.equalTo(ORGANIZATION_OVERRIDE)));
        bucketCaptor.getAllValues().forEach(v -> assertThat(v, CoreMatchers.equalTo(BUCKET_OVERRIDE)));
    }

    @Test
    void testAddHostName() {
        when(config.host_name_add()).thenReturn(true);

        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testAddHostAddress() {
        when(config.host_address_add()).thenReturn(true);

        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testOsgiEventTimestamp() {
        when(config.timestamp_add()).thenReturn(true);
        when(config.timestamp_precision()).thenReturn("ns");

        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        point.time((Long) payload.get(EventConstants.TIMESTAMP) * 1000000, WritePrecision.NS);
        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testSystemTimestamp() {
        when(config.timestamp_add()).thenReturn(true);
        when(config.timestamp_precision()).thenReturn("us");
        payload.remove(EventConstants.TIMESTAMP);

        final Point point = POINT1_WITHOUT_TIMESTAMP.get().time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        final long startTs = System.currentTimeMillis() * 1000;
        testPointWriter();
        final long endTs = (System.currentTimeMillis() + 1) * 1000;

        verify(writeApi).writePoint(pointCaptor.capture());
        final String originalWithoutTimestamp = point.toLineProtocol().replaceFirst(" [0-9]+$", "");
        final String written = pointCaptor.getValue().toLineProtocol();
        assertThat(written, startsWith(originalWithoutTimestamp));
        final Long timestamp = Long.parseLong(written.replace(originalWithoutTimestamp + " ", ""));
        assertThat(timestamp, allOf(greaterThanOrEqualTo(startTs), lessThanOrEqualTo(endTs)));
    }
}
