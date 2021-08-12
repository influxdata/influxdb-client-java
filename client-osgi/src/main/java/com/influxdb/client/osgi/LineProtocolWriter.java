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

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.internal.NanosecondConverter;

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

/**
 * OSGi event handler writing InfluxDB line protocol records.
 *
 * <p>Service is used to write RAW (line protocol) event data to InfluxDB. OSGi event topic
 * {@link LineProtocolWriter#DEFAULT_EVENT_TOPIC} used by default. The following OSGi event properties are
 * supported:</p>
 *
 * <ul>
 *     <li><i>record</i> ({@link String}): value must be a single line protocol record</li>
 *     <li><i>records</i> ({@link List}): value must be a list of line protocol records</li>
 *     <li><i>precision</i> ({@link String}, optional): write precision, values: <tt>s</tt>, <tt>ms</tt>, <tt>us</tt>,
 *         <tt>ns</tt>, default: <tt>ns</tt></li>
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
     * OSGi event property name used to write list of line protocol records.
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
        final WriteApiBlocking writeApi = client.getWriteApiBlocking();

        final String record = (String) event.getProperty(RECORD);
        final List<String> records = (List<String>) event.getProperty(RECORDS);
        final BigInteger timestamp = calculateTimestamp(event, writePrecision);
        if (record != null) {
            final String recordToWrite = timestamp != null ? record + " " + timestamp : record;
            if (organization != null && bucket != null) {
                writeApi.writeRecord(bucket, organization, writePrecision, recordToWrite);
            } else {
                writeApi.writeRecord(writePrecision, recordToWrite);
            }
        } else if (records != null) {
            final List<String> recordsToWrite = timestamp != null
                    ? records.stream().map(r -> r + " " + timestamp).collect(Collectors.toList())
                    : records;
            if (organization != null && bucket != null) {
                writeApi.writeRecords(bucket, organization, writePrecision, recordsToWrite);
            } else {
                writeApi.writeRecords(writePrecision, recordsToWrite);
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
            writePrecision = WritePrecision.NS;
        }

        return writePrecision;
    }

    private BigInteger calculateTimestamp(final Event event, final WritePrecision precision) {
        if (config.timestamp_append()) {
            final Long timestamp = (Long) event.getProperty(EventConstants.TIMESTAMP);
            if (timestamp != null) {
                return NanosecondConverter.convert(timestamp, precision);
            } else {
                return NanosecondConverter.currentTimestamp(precision);
            }
        } else {
            return null;
        }
    }
}
