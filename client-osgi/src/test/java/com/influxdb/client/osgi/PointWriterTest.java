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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
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

import java.net.InetAddress;
import java.time.*;
import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointWriterTest {

    private PointWriter pointWriter;

    private static final String EVENT_TOPIC = "influxdb/weather";
    private static final Point POINT1_WITHOUT_TIMESTAMP = Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.89)
            .addField("humidity", 48);
    private static final Point POINT2_WITHOUT_TIMESTAMP = Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.93)
            .addField("humidity", 48);
    private static final Point POINT3_WITHOUT_TIMESTAMP = Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.90)
            .addField("humidity", 52);
    private static final Point POINT4_WITHOUT_TIMESTAMP = Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 6.03)
            .addField("humidity", 53);
    private static final Point POINT5_WITHOUT_TIMESTAMP = Point.measurement("weather")
            .addTag("location", "HU")
            .addField("temperature", 5.98)
            .addField("humidity", 47);
    private static final Map<String, Object> MAP1_WITHOUT_TIMESTAMP = new TreeMap<>();
    private static final String ORGANIZATION_OVERRIDE = "my-org";
    private static final String BUCKET_OVERRIDE = "my-bucket";

    static {
        MAP1_WITHOUT_TIMESTAMP.put("location", "HU");
        MAP1_WITHOUT_TIMESTAMP.put("temperature", 5.89);
        MAP1_WITHOUT_TIMESTAMP.put("humidity", 48);
    }

    @Mock
    private WriteApi writeApi;

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
        when(client.getWriteApi()).thenReturn(writeApi);
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
        final Point point = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
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
        when(config.host_name_add()).thenReturn(true);

        MAP1_WITHOUT_TIMESTAMP.put("raining", false);
        MAP1_WITHOUT_TIMESTAMP.put("date", LocalDate.now());
        final Map<String, Object> map = new TreeMap<>(MAP1_WITHOUT_TIMESTAMP);
        payload.put(PointWriter.POINT, map);

        final long startTs = (Long) payload.get(EventConstants.TIMESTAMP);

        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, System.currentTimeMillis());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, new Date());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, LocalDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, OffsetDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, ZonedDateTime.now());
        testPointWriter();

        map.put(PointWriter.TIMESTAMP_NAME, Instant.now());
        testPointWriter();

        map.remove(PointWriter.TIMESTAMP_NAME);
        payload.remove(EventConstants.TIMESTAMP);
        testPointWriter();

        final long endTs = System.currentTimeMillis();

        verify(writeApi, times(8)).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getAllValues(), hasSize(8));

        final Point point = POINT1_WITHOUT_TIMESTAMP.addField("raining", false);
        final String originalWithoutTimestamp = point.toLineProtocol().replaceFirst(" [0-9]+$", "");

        IntStream.rangeClosed(0, 7).forEach(i -> {
            final String written = pointCaptor.getAllValues().get(i).toLineProtocol();
            final Long timestamp = Long.parseLong(written.replace(originalWithoutTimestamp + " ", ""));
            assertThat(timestamp, allOf(greaterThanOrEqualTo(startTs), lessThanOrEqualTo(endTs)));
        });
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
        final Point point1 = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
        final Point point2 = POINT2_WITHOUT_TIMESTAMP.time(1617826120000L, WritePrecision.MS);
        final Point point3 = POINT3_WITHOUT_TIMESTAMP.time(1617826121000L, WritePrecision.MS);
        final Point point4 = POINT4_WITHOUT_TIMESTAMP.time(1617826122000L, WritePrecision.MS);
        final Point point5 = POINT5_WITHOUT_TIMESTAMP.time(1617826123000L, WritePrecision.MS);

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
    void testAddHostName() throws Exception {
        when(config.host_name_add()).thenReturn(true);

        final Point point = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        point.addField(PointWriter.HOST_NAME, InetAddress.getLocalHost().getHostName());
        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testAddHostAddress() throws Exception {
        when(config.host_address_add()).thenReturn(true);

        final Point point = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        point.addField(PointWriter.HOST_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testOsgiEventTimestamp() {
        when(config.timestamp_add()).thenReturn(true);

        final Point point = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
        payload.put(PointWriter.POINT, point);

        testPointWriter();

        point.time((Long) payload.get(EventConstants.TIMESTAMP), WritePrecision.MS);
        verify(writeApi).writePoint(pointCaptor.capture());
        assertThat(pointCaptor.getValue(), equalTo(point));
    }

    @Test
    void testSystemTimestamp() {
        when(config.timestamp_add()).thenReturn(true);
        when(config.timestamp_precision()).thenReturn("us");
        payload.remove(EventConstants.TIMESTAMP);

        final Point point = POINT1_WITHOUT_TIMESTAMP.time(1617826119000L, WritePrecision.MS);
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
