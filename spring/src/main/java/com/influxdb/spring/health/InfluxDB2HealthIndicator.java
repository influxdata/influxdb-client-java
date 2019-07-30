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
package com.influxdb.spring.health;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.Check;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.Assert;

/**
 * {@link HealthIndicator} for InfluxDB 2.
 *
 * @author Jakub Bednar (bednar@github)
 */
public class InfluxDB2HealthIndicator extends AbstractHealthIndicator {

    private final InfluxDBClient influxDBClient;

    public InfluxDB2HealthIndicator(final InfluxDBClient influxDBClient) {
        super("InfluxDBClient 2 health check failed");
        Assert.notNull(influxDBClient, "InfluxDBClient must not be null");

        this.influxDBClient = influxDBClient;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        Check check = this.influxDBClient.health();

        switch (check.getStatus()) {
            case PASS:
                builder.up();
                break;
            case FAIL:
                builder.down();
                break;
            default:
                builder.unknown();
        }

        builder
                .withDetail("status", check.getStatus())
                .withDetail("message", check.getMessage());
    }
}
