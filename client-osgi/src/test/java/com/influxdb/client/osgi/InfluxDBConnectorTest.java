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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InfluxDBConnectorTest {

    private static final String ALIAS = "test";
    private static final String URL_V1 = "http://localhost:8086";
    private static final String URL_V2 = "http://localhost:9999";
    private static final String TOKEN = "my-token";
    private static final String USERNAME = "my-user";
    private static final String PASSWORD = "my-password";
    private static final String ORGANIZATION = "my-organization";
    private static final String BUCKET = "my-bucket";
    private static final String DATABASE = "my-database";
    private static final String RETENTION_POLICY = "autogen";

    private InfluxDBConnector influxDBConnector;

    @Mock
    private BundleContext context;

    @Mock
    private InfluxDBConnector.Config config;

    @Mock
    private ServiceRegistration<InfluxDBClient> serviceRegistration;

    @Captor
    ArgumentCaptor<InfluxDBClient> client;

    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> serviceProperties;

    @BeforeEach
    void setUp() {
        influxDBConnector = new InfluxDBConnector();
        when(config.alias()).thenReturn(ALIAS);
    }

    void startConnector() {
        influxDBConnector.start(context, config);
    }

    void stopConnector() {
        influxDBConnector.stop();
    }

    @Test
    void testConnectorWithToken() {
        when(config.url()).thenReturn(URL_V2);
        when(config.token()).thenReturn(TOKEN);
        when(config.organization()).thenReturn(ORGANIZATION);
        when(config.bucket()).thenReturn(BUCKET);

        when(context.registerService((Class) notNull(), (InfluxDBConnector) notNull(), notNull()))
                .thenReturn(serviceRegistration);

        startConnector();

        verify(context).registerService(any(Class.class), client.capture(), serviceProperties.capture());
        final Dictionary<String, Object> expectedProperties = new Hashtable<>();
        expectedProperties.put(InfluxDBConnector.URL, URL_V2);
        expectedProperties.put(InfluxDBConnector.ALIAS, ALIAS);
        expectedProperties.put(InfluxDBConnector.ORGANIZATION, ORGANIZATION);
        expectedProperties.put(InfluxDBConnector.BUCKET, BUCKET);
        assertThat(serviceProperties.getValue(), equalTo(expectedProperties));

        stopConnector();
        verify(serviceRegistration).unregister();
    }

    @Test
    void testActivateFailed() {
        when(config.url()).thenReturn(URL_V2);
        when(config.token()).thenReturn(TOKEN);
        when(config.organization()).thenReturn(ORGANIZATION);
        when(config.bucket()).thenReturn(BUCKET);

        when(context.registerService((Class) notNull(), (InfluxDBConnector) notNull(), notNull()))
                .thenThrow(new IllegalStateException("Bundle context is invalid"));

        Assertions.assertThrows(IllegalStateException.class, () -> startConnector());
    }

    @Test
    void testConnectorV1() {
        when(config.url()).thenReturn(URL_V1);
        when(config.username()).thenReturn(USERNAME);
        when(config.password()).thenReturn(PASSWORD);
        when(config.database()).thenReturn(DATABASE);
        when(config.retentionPolicy()).thenReturn(RETENTION_POLICY);

        when(context.registerService((Class) notNull(), (InfluxDBConnector) notNull(), notNull()))
                .thenReturn(serviceRegistration);

        startConnector();

        verify(context).registerService(any(Class.class), client.capture(), serviceProperties.capture());
        final Dictionary<String, Object> expectedProperties = new Hashtable<>();
        expectedProperties.put(InfluxDBConnector.URL, URL_V1);
        expectedProperties.put(InfluxDBConnector.ALIAS, ALIAS);
        expectedProperties.put(InfluxDBConnector.DATABASE, DATABASE);
        assertThat(serviceProperties.getValue(), equalTo(expectedProperties));

        stopConnector();
        verify(serviceRegistration).unregister();
    }

    @Test
    void testConnectorWithUsernameAndPassword() {
        when(config.url()).thenReturn(URL_V2);
        when(config.alias()).thenReturn(null);
        when(config.username()).thenReturn(USERNAME);
        when(config.password()).thenReturn(PASSWORD);

        when(context.registerService((Class) notNull(), (InfluxDBConnector) notNull(), notNull()))
                .thenReturn(serviceRegistration);

        startConnector();

        verify(context).registerService(any(Class.class), client.capture(), serviceProperties.capture());
        final Dictionary<String, Object> expectedProperties = new Hashtable<>();
        expectedProperties.put(InfluxDBConnector.URL, URL_V2);
        assertThat(serviceProperties.getValue(), equalTo(expectedProperties));

        stopConnector();
        verify(serviceRegistration).unregister();
    }
}
