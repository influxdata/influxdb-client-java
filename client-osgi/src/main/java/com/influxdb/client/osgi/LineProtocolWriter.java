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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

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
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static java.util.function.Function.identity;

/**
 * OSGi event handler writing InfluxDB line protocol records.
 *
 * <p>Service is used to write RAW (line protocol) event data to InfluxDB. OSGi event topic
 * {@link LineProtocolWriter#DEFAULT_EVENT_TOPIC} used by default. The following OSGi event properties are
 * supported:</p>
 *
 * <ul>
 *     <li><i>record</i> ({@link String}): value must be a single line protocol record</li>
 *     <li><i>records</i> ({@link Collection}): value must be a collection of line protocol records</li>
 *     <li><i>precision</i> ({@link String}, optional): write precision, values: <tt>s</tt>, <tt>ms</tt>, <tt>us</tt>,
 *         <tt>ns</tt>, default: <tt>ms</tt></li>
 *     <li><i>organization</i> ({@link String}, optional): used to override InfluxDB organization of
 *         {@link LineProtocolWriter} service (by event).</li>
 *     <li><i>bucket</i> ({@link String}, optional): used to override InfluxDB bucket of
 *         {@link LineProtocolWriter} service (by event).</li>
 * </ul>
 *
 * <p>One of <i>record</i> or <i>records</i> must be set.</p>
 *
 * <p>Timestamp is appended to the line protocol record if it is enabled by configuration option (in that case records
 * must not contain timestamp data!). Value is read from OSGi event (property {@link EventConstants#TIMESTAMP}) or
 * {@link System#currentTimeMillis()} is set if property not found.</p>
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
        EventConstants.EVENT_TOPIC + "=" + LineProtocolWriter.DEFAULT_EVENT_TOPIC
})
public class LineProtocolWriter implements EventHandler {

    /**
     * Default OSGi event topic.
     */
    public static final String DEFAULT_EVENT_TOPIC = "influxdb/lineprotocol";

    /**
     * OSGi event property name used to write single line protocol record.
     */
    public static final String RECORD = "record";
    /**
     * OSGi event property name used to write collection of line protocol records.
     */
    public static final String RECORDS = "records";
    /**
     * OSGi event property name to set precision.
     */
    public static final String PRECISION = "precision";

    /**
     * OSGi event property name to override InfluxDB organization.
     */
    public static final String ORGANIZATION = "organization";
    /**
     * OSGi event property name to override InfluxDB bucket.
     */
    public static final String BUCKET = "bucket";

    private static final long THOUSAND = 1000L;
    private static final long MILLION = 1000000L;

    /**
     * Timestamp calculation functions to add timestamp to records.
     */
    private static final Map<WritePrecision, Function<Long, Long>> TIMESTAMP_CALCULATIONS = new HashMap<>();

    static {
        TIMESTAMP_CALCULATIONS.put(WritePrecision.S, (timestamp) -> timestamp / THOUSAND);
        TIMESTAMP_CALCULATIONS.put(WritePrecision.MS, identity());
        TIMESTAMP_CALCULATIONS.put(WritePrecision.US, (timestamp) -> timestamp * THOUSAND);
        TIMESTAMP_CALCULATIONS.put(WritePrecision.NS, (timestamp) -> timestamp * MILLION);
    }

    /**
     * Configuration for Line Protocol Writer.
     */
    @ObjectClassDefinition(name = "InfluxDB Line Protocol Writer",
            description = "Event handler writing line protocol record(s) to InfluxDB")
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
         * Current timestamp is appended to line protocol record(s) is enabled.
         */
        @AttributeDefinition(required = false, name = "Append timestamp",
                description = "Append timestamp to line protocol record(s)", type = AttributeType.BOOLEAN)
        boolean timestamp_append() default false;

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
     * Write line protocol record from OSGi event.
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

        final WritePrecision writePrecision = getPrecision(event);
        final WriteApi writeApi = client.getWriteApi();

        final String record = (String) event.getProperty(RECORD);
        final Collection<String> records = (Collection<String>) event.getProperty(RECORDS);
        if (record != null) {
            if (organization != null && bucket != null) {
                writeApi.writeRecord(bucket, organization, writePrecision, decorate(record, writePrecision, event));
            } else {
                writeApi.writeRecord(writePrecision, decorate(record, writePrecision, event));
            }
        } else if (records != null) {
            if (organization != null && bucket != null) {
                writeApi.writeRecords(bucket, organization, writePrecision, records.stream()
                        .map(r -> decorate(r, writePrecision, event))
                        .collect(Collectors.toList()));
            } else {
                writeApi.writeRecords(writePrecision, records.stream()
                        .map(r -> decorate(r, writePrecision, event))
                        .collect(Collectors.toList()));
            }
        } else {
            throw new IllegalArgumentException("Missing line protocol record(s)");
        }
    }

    private WritePrecision getPrecision(final Event event) {
        final Object precision = event.getProperty(PRECISION);
        final WritePrecision writePrecision;
        if (precision instanceof String) {
            writePrecision = WritePrecision.fromValue((String) precision);
            if (writePrecision == null) {
                throw new IllegalArgumentException("Unsupported precision");
            }
        } else if (precision instanceof WritePrecision) {
            writePrecision = (WritePrecision) precision;
        } else {
            writePrecision = WritePrecision.MS;
        }

        return writePrecision;
    }

    private String decorate(final String record, final WritePrecision writePrecision, final Event event) {
        if (config.timestamp_append()) {
            final Long timestamp = (Long) event.getProperty(EventConstants.TIMESTAMP);
            if (timestamp != null) {
                return record + " " + TIMESTAMP_CALCULATIONS.get(writePrecision).apply(timestamp);
            } else {
                return record + " " + TIMESTAMP_CALCULATIONS.get(writePrecision).apply(System.currentTimeMillis());
            }
        } else {
            return record;
        }
    }
}
