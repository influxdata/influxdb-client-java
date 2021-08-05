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

import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi event handler writing structured data (points) to InfluxDB.
 *
 * <p>Service is used to write structural ({@link com.influxdb.client.write.Point} or
 * {@link java.util.Map}) event data to InfluxDB. OSGi event topic
 * {@link PointWriter#DEFAULT_EVENT_TOPIC} used by default. The following OSGi event properties are supported:</p>
 *
 * <ul>
 *     <li><i>point</i> ({@link Point} or {@link Map}): value represents a single structured data</li>
 *     <li><i>points</i> ({@link Collection}): value represents a collection of structured data</li>
 *     <li><i>organization</i> ({@link String}, optional): used to override InfluxDB organization of
 *         {@link LineProtocolWriter} service (by event).</li>
 *     <li><i>bucket</i> ({@link String}, optional): used to override InfluxDB bucket of
 *         {@link LineProtocolWriter} service (by event).</li>
 * </ul>
 *
 * <p>One of <i>point</i> or <i>points</i> must be set.</p>
 *
 * <p>Measurement name is defined by OSGi event topic if {@link Map} type is used: measure name is fragment after the
 * last slash (<tt>/</tt>) character. For example: <b>weather</b> measure is written if event topic is
 * <tt>influxdb/point/weather</tt> (in this case OSGi event filter must be changed, i.e. <tt>influxdb/point/*</tt>).</p>
 *
 * <p>Structured data can be decorated with host name, host address or timestamp (by configuration).</p>
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
        EventConstants.EVENT_TOPIC + "=" + PointWriter.DEFAULT_EVENT_TOPIC
})
@Designate(ocd = PointWriter.Config.class)
@Slf4j
public class PointWriter implements EventHandler {

    /**
     * Default OSGi event topic.
     */
    public static final String DEFAULT_EVENT_TOPIC = "influxdb/point";

    /**
     * OSGi event property name used to write single structured data.
     */
    public static final String POINT = "point";
    /**
     * OSGi event property name used to write collection of structured data.
     */
    public static final String POINTS = "points";

    /**
     * OSGi event property name to override InfluxDB organization.
     */
    public static final String ORGANIZATION = "organization";
    /**
     * OSGi event property name to override InfluxDB bucket.
     */
    public static final String BUCKET = "bucket";

    /**
     * Field name used to store host name.
     */
    static final String HOST_NAME = "_host";

    /**
     * Field name used to store host address.
     */
    static final String HOST_ADDRESS = "_host_address";

    /**
     * Field name used to store timestamp.
     */
    static final String TIMESTAMP_KEY = "timestamp";

    /**
     * Field name used for tags.
     */
    static final String TAGS_KEY = "tags";

    /**
     * Field name used for fields.
     */
    static final String FIELDS_KEY = "fields";

    /**
     * Configuration for Point Writer.
     */
    @ObjectClassDefinition(name = "InfluxDB Point Writer",
            description = "Event handler writing point(s) to InfluxDB")
    public @interface Config {

        /**
         * OSGi event handler topic(s).
         */
        @AttributeDefinition(name = "Topics",
                description = "OSGi event topics")
        String[] event_topics() default {DEFAULT_EVENT_TOPIC};

        /**
         * OSGi target filter for InfluxDB connection, i.e. <tt>(alias=test)</tt>. The following properties are
         * copied from {@link InfluxDBConnector}: <tt>organization</tt>, <tt>bucket</tt>, <tt>database</tt>,
         * <tt>url</tt>, <tt>alias</tt>.
         */
        @AttributeDefinition(required = false, name = "InfluxDB client target",
                description = "OSGi target filter of InfluxDB client service")
        String client_target();

        /**
         * Tag point(s) by host name (as {@link PointWriter#HOST_NAME}) if enabled.
         */
        @AttributeDefinition(required = false, name = "Add host name",
                description = "Add host name to point(s)", type = AttributeType.BOOLEAN)
        boolean host_name_add() default false;

        /**
         * Tag point(s) by host address (as {@link PointWriter#HOST_ADDRESS}) if enabled.
         */
        @AttributeDefinition(required = false, name = "Add host address",
                description = "Add host address to point(s)", type = AttributeType.BOOLEAN)
        boolean host_address_add() default false;

        /**
         * Add timestamp to point(s) if enabled. Timestamp of OSGi event is used if available, current timestamp
         * otherwise.
         */
        @AttributeDefinition(required = false, name = "Add timestamp",
                description = "Add timestamp to point(s)", type = AttributeType.BOOLEAN)
        boolean timestamp_add() default false;

        /**
         * Precision used if adding timestamp, values: <tt>s</tt>, <tt>ms</tt>, <tt>us</tt>, <tt>ns</tt>.
         */
        @AttributeDefinition(required = false, name = "Precision",
                description = "Precision used if adding timestamp")
        String timestamp_precision() default "ns";

        /**
         * InfluxDB organization to write data (overriding organization of {@link InfluxDBClient}).
         */
        @AttributeDefinition(required = false, name = "Organization",
                description = "InfluxDB organization to write")
        String organization();

        /**
         * InfluxDB bucket to write data (overriding bucket of {@link InfluxDBClient}).
         */
        @AttributeDefinition(required = false, name = "Bucket",
                description = "InfluxDB bucket to write")
        String bucket();
    }

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    InfluxDBClient client;

    Config config;

    /**
     * Start or reconfigure OSGi component.
     *
     * @param config configuration
     */
    @Activate
    @Modified
    void start(final Config config) {
        this.config = config;
    }

    /**
     * Write structured data from OSGi event.
     *
     * @param event OSGI event containing data
     */
    @Override
    public void handleEvent(final Event event) {
        String organization = (String) event.getProperty(ORGANIZATION);
        if (organization == null) {
            organization = config.organization();
        }
        String bucket = (String) event.getProperty(BUCKET);
        if (bucket == null) {
            bucket = config.bucket();
        }

        final WriteApiBlocking writeApi = client.getWriteApiBlocking();

        final Object point = event.getProperty(POINT);
        final Collection<Object> points = (Collection<Object>) event.getProperty(POINTS);

        final Instant timestamp;
        if (config.timestamp_add()) {
            final Long epoch = (Long) event.getProperty(EventConstants.TIMESTAMP);
            if (epoch != null) {
                timestamp = new Date(epoch).toInstant();
            } else {
                timestamp = Instant.now();
            }
        } else {
            timestamp = null;
        }

        if (point != null) {
            if (organization != null && bucket != null) {
                writeApi.writePoint(bucket, organization, decorate(point, event, timestamp));
            } else {
                writeApi.writePoint(decorate(point, event, timestamp));
            }
        } else if (points != null) {
            if (organization != null && bucket != null) {
                writeApi.writePoints(bucket, organization, points.stream()
                        .map(p -> decorate(p, event, timestamp))
                        .collect(Collectors.toList()));
            } else {
                writeApi.writePoints(points.stream()
                        .map(p -> decorate(p, event, timestamp))
                        .collect(Collectors.toList()));
            }
        } else {
            throw new IllegalArgumentException("Missing point(s)");
        }
    }

    @SneakyThrows
    private Point decorate(final Object object, final Event event, final Instant instant) {
        final WritePrecision precision = WritePrecision.fromValue(config.timestamp_precision());

        final Point point;
        if (object instanceof Point) {
            point = (Point) object;
            if (instant != null) {
                point.time(instant, precision);
            }
        } else if (object instanceof Map) {
            final String measurement = event.getTopic().replaceAll(".*/", "");
            point = Point.measurement(measurement);

            final Map<String, Object> data = (Map<String, Object>) object;

            final Object timestamp = data.get(TIMESTAMP_KEY);
            if (instant != null) {
                point.time(instant, precision);
            } else if (!setTimestamp(timestamp, point, precision)) {
                // instant must be set because no (valid) _timestamp found in map
                point.time(Instant.now(), precision);
            }

            Optional.ofNullable((Map<String, String>) data.get(TAGS_KEY))
                    .ifPresent(tags -> point.addTags(tags));

            Optional.ofNullable((Map<String, Object>) data.get(FIELDS_KEY))
                    .ifPresent(fields -> point.addFields(fields));
        } else {
            throw new IllegalArgumentException("Invalid point");
        }

        if (config.host_name_add()) {
            point.addTag(HOST_NAME, InetAddress.getLocalHost().getHostName());
            point.addField(HOST_NAME, InetAddress.getLocalHost().getHostName());
        }

        if (config.host_address_add()) {
            point.addTag(HOST_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        }

        return point;
    }

    private boolean setTimestamp(final Object timestamp, final Point point, final WritePrecision precision) {
        if (timestamp instanceof Date) {
            point.time(((Date) timestamp).toInstant(), precision);
        } else if (timestamp instanceof Instant) {
            point.time((Instant) timestamp, precision);
        } else if (timestamp instanceof LocalDateTime) {
            point.time(((LocalDateTime) timestamp).atZone(ZoneId.systemDefault()).toInstant(), precision);
        } else if (timestamp instanceof OffsetDateTime) {
            point.time(((OffsetDateTime) timestamp).toInstant(), precision);
        } else if (timestamp instanceof ZonedDateTime) {
            point.time(((ZonedDateTime) timestamp).toInstant(), precision);
        } else if (timestamp instanceof Long) {
            point.time((Long) timestamp, precision);
        } else {
            return false;
        }

        return true;
    }
}
