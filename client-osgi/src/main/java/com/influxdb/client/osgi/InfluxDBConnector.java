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

import java.util.Dictionary;
import java.util.Hashtable;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * InfluxDB connector managing {@link InfluxDBClient} OSGi service.
 *
 * <p>Service is used to define an InfluxDB connection. A {@link com.influxdb.client.InfluxDBClient} service is
 * registered by the connector, and it can be referenced by any custom OSGi component.</p>
 *
 * <ol>
 *     <li>InfluxDB V2 client is created if token is set.</li>
 *     <li>InfluxDB V1 (V1.7+) client is created if token is not set but database.</li>
 *     <li>InfluxDB V2 client is created (by username and password) otherwise.</li>
 * </ol>
 *
 * <p>The following connector properties are added to InfluxDB client too: <code>organization</code>,
 * <code>bucket</code>, <code>database</code>, <code>url</code> and <code>alias</code>.</p>
 */
@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, service = InfluxDBConnector.class)
@Designate(ocd = InfluxDBConnector.Config.class)
public class InfluxDBConnector {

    static final String URL = "url";
    static final String ORGANIZATION = "organization";
    static final String BUCKET = "bucket";
    static final String DATABASE = "database";
    static final String ALIAS = "alias";

    /**
     * Configuration for InfluxDB connector.
     */
    @ObjectClassDefinition(name = "InfluxDB connector",
            description = "InfluxDB connector configuration options")
    public @interface Config {

        /**
         * Alias (discriminator) to select {@link InfluxDBClient} service instance by OSGi target filter.
         */
        @AttributeDefinition(name = "InfluxDB connector alias", description = "Alias that can be used by OSGi filters")
        String alias();

        /**
         * InfluxDB URL, i.e. <code>http://localhost:8086</code>
         */
        @AttributeDefinition(name = "InfluxDB URL")
        String url();

        /**
         * InfluxDB token.
         */
        @AttributeDefinition(required = false, name = "InfluxDB token", type = AttributeType.PASSWORD)
        String token();

        /**
         * InfluxDB organization.
         */
        @AttributeDefinition(required = false, name = "InfluxDB organization")
        String organization();

        /**
         * InfluxDB bucket.
         */
        @AttributeDefinition(required = false, name = "InfluxDB bucket")
        String bucket();

        /**
         * InfluxDB username.
         */
        @AttributeDefinition(required = false, name = "InfluxDB username")
        String username();

        /**
         * InfluxDB password.
         */
        @AttributeDefinition(required = false, name = "InfluxDB password", type = AttributeType.PASSWORD)
        String password();

        /**
         * InfluxDB (V1) database.
         */
        @Deprecated
        @AttributeDefinition(required = false, name = "InfluxDB (V1) database")
        String database();

        /**
         * InfluxDB (V1) retention policy.
         */
        @Deprecated
        @AttributeDefinition(required = false, name = "InfluxDB (V1) retention policy")
        String retentionPolicy();
    }

    private Config config;

    InfluxDBClient client;
    ServiceRegistration<InfluxDBClient> clientServiceRegistration;

    /**
     * Start OSGi component.
     *
     * @param bundleContext OSGi bundle context
     * @param config        configuration
     */
    @Activate
    void start(final BundleContext bundleContext, final Config config) {
        this.config = config;

        if (config.token() != null) {
            client = InfluxDBClientFactory.create(
                    config.url(),
                    config.token().toCharArray(),
                    config.organization(),
                    config.bucket());
        } else if (config.database() != null) {
            client = InfluxDBClientFactory.createV1(
                    config.url(),
                    config.username(),
                    config.password().toCharArray(),
                    config.database(),
                    config.retentionPolicy());
        } else {
            client = InfluxDBClientFactory.create(
                    config.url(),
                    config.username(),
                    config.password().toCharArray());
        }

        try {
            clientServiceRegistration = bundleContext.registerService(
                    InfluxDBClient.class,
                    client,
                    getServiceProperties());
        } catch (RuntimeException ex) {
            client.close();
            throw ex;
        }
    }

    /**
     * Stop OSGi component.
     */
    @Deactivate
    void stop() {
        try {
            clientServiceRegistration.unregister();
            client.close();
        } finally {
            clientServiceRegistration = null;
            client = null;
        }
    }

    private Dictionary<String, Object> getServiceProperties() {
        final Dictionary<String, Object> props = new Hashtable<>();

        props.put(URL, config.url());
        if (config.organization() != null) {
            props.put(ORGANIZATION, config.organization());
        }
        if (config.bucket() != null) {
            props.put(BUCKET, config.bucket());
        }
        if (config.database() != null) {
            props.put(DATABASE, config.database());
        }
        if (config.alias() != null) {
            props.put(ALIAS, config.alias());
        }

        return props;
    }
}
