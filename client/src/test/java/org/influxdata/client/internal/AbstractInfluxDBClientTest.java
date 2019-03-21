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
package org.influxdata.client.internal;

import java.util.Optional;
import javax.annotation.Nonnull;

import org.influxdata.client.InfluxDBClient;
import org.influxdata.client.InfluxDBClientFactory;
import org.influxdata.client.WriteApi;
import org.influxdata.client.WriteOptions;
import org.influxdata.test.AbstractMockServerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 12:29)
 */
public class AbstractInfluxDBClientTest extends AbstractMockServerTest {

    protected InfluxDBClient influxDBClient;

    @BeforeEach
    protected void setUp() {

        influxDBClient = InfluxDBClientFactory.create(startMockServer());
    }

    @Nonnull
    protected WriteApi createWriteClient(@Nonnull final WriteOptions writeOptions) {

        Optional<Object> influxDBService = ReflectionUtils.readFieldValue(AbstractInfluxDBClient.class, "influxDBService",
                (AbstractInfluxDBClient) influxDBClient);

        if (!influxDBService.isPresent()) {
            Assertions.fail();
        }

        return new WriteApiImpl(writeOptions, null);
    }
}