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
package com.influxdb.client.write;

import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.client.domain.WritePrecision;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
class WriteParametersTest {

    private InfluxDBClientOptions.Builder optionsBuilder;

    @BeforeEach
    void before() {
        optionsBuilder = InfluxDBClientOptions
                .builder()
                .url("http://localhost:8086")
                .authenticateToken("my-token".toCharArray());
    }

    @Test
    void check() {
        new WriteParameters("my-bucket", "my-org", WritePrecision.NS).check(optionsBuilder.build());

        Object[][] tests = {
                new Object[]{new WriteParameters("my-bucket", "my-org", WritePrecision.NS), optionsBuilder.build(), null},
                new Object[]{new WriteParameters("my-bucket", "my-org", WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.build(), null},
                new Object[]{new WriteParameters("my-bucket", null, WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.build(), "Expecting a non-empty string for destination organization. Please specify the organization as a method parameter or use default configuration at 'InfluxDBClientOptions.Organization'."},
                new Object[]{new WriteParameters("my-bucket", "", WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.build(), "Expecting a non-empty string for destination organization. Please specify the organization as a method parameter or use default configuration at 'InfluxDBClientOptions.Organization'."},
                new Object[]{new WriteParameters("my-bucket", null, WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.org("my-org").build(), null},
                new Object[]{new WriteParameters(null, "my-org", WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.build(), "Expecting a non-empty string for destination bucket. Please specify the bucket as a method parameter or use default configuration at 'InfluxDBClientOptions.Bucket'."},
                new Object[]{new WriteParameters("", "my-org", WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.build(), "Expecting a non-empty string for destination bucket. Please specify the bucket as a method parameter or use default configuration at 'InfluxDBClientOptions.Bucket'."},
                new Object[]{new WriteParameters(null, "my-org", WritePrecision.NS, WriteConsistency.ONE), optionsBuilder.bucket("my-bucket").build(), null},
        };

        for (Object[] test : tests) {
            WriteParameters parameters = (WriteParameters) test[0];
            InfluxDBClientOptions options = (InfluxDBClientOptions) test[1];
            String message = (String) test[2];

            if (message == null) {
                parameters.check(options);
            } else {
                Assertions.assertThatThrownBy(() -> parameters.check(options))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage(message);
            }
        }
    }

    @Test
    void optionParameters() {
        InfluxDBClientOptions options = optionsBuilder
                .bucket("my-bucket")
                .org("my-org")
                .precision(WritePrecision.S)
                .consistency(WriteConsistency.ANY)
                .build();

        WriteParameters parameters = new WriteParameters(null, null, null, null);

        Assertions.assertThat(parameters.bucketSafe(options)).isEqualTo("my-bucket");
        Assertions.assertThat(parameters.orgSafe(options)).isEqualTo("my-org");
        Assertions.assertThat(parameters.precisionSafe(options)).isEqualTo(WritePrecision.S);
        Assertions.assertThat(parameters.consistencySafe(options)).isEqualTo(WriteConsistency.ANY);
    }

    @Test
    void nullableParameters() {
        InfluxDBClientOptions options = optionsBuilder.bucket("my-bucket").org("my-org").build();

        WriteParameters parameters = new WriteParameters(null, null, null, null);

        Assertions.assertThat(parameters.precisionSafe(options)).isEqualTo(WritePrecision.NS);
        Assertions.assertThat(parameters.consistencySafe(options)).isNull();
    }

    @Test
    void v1Constructor() {
        InfluxDBClientOptions options = optionsBuilder.bucket("my-bucket").org("my-org").build();

        WriteParameters parameters = new WriteParameters(WritePrecision.NS, WriteConsistency.ONE);

        Assertions.assertThat(parameters.orgSafe(options)).isEqualTo("my-org");
        Assertions.assertThat(parameters.bucketSafe(options)).isEqualTo("my-bucket");
        Assertions.assertThat(parameters.precisionSafe(options)).isEqualTo(WritePrecision.NS);
        Assertions.assertThat(parameters.consistencySafe(options)).isEqualTo(WriteConsistency.ONE);
    }
}
