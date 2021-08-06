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
package com.influxdb.client.reactive;

import io.reactivex.schedulers.Schedulers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (05/08/2021 9:23)
 */
@RunWith(JUnitPlatform.class)
class WriteOptionsReactiveTest {
    @Test
    void configure() {

        WriteOptionsReactive writeOptions = WriteOptionsReactive.builder()
                .batchSize(10_000)
                .flushInterval(500)
                .jitterInterval(1_000)
                .retryInterval(2_000)
                .maxRetries(5)
                .maxRetryDelay(250_123)
                .maxRetryTime(500_000)
                .exponentialBase(2)
                .computationScheduler(Schedulers.newThread())
                .build();

        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(10_000);
        Assertions.assertThat(writeOptions.getFlushInterval()).isEqualTo(500);
        Assertions.assertThat(writeOptions.getJitterInterval()).isEqualTo(1_000);
        Assertions.assertThat(writeOptions.getRetryInterval()).isEqualTo(2_000);
        Assertions.assertThat(writeOptions.getMaxRetries()).isEqualTo(5);
        Assertions.assertThat(writeOptions.getMaxRetryDelay()).isEqualTo(250_123);
        Assertions.assertThat(writeOptions.getMaxRetryTime()).isEqualTo(500_000);
        Assertions.assertThat(writeOptions.getExponentialBase()).isEqualTo(2);
        Assertions.assertThat(writeOptions.getComputationScheduler()).isEqualTo(Schedulers.newThread());
    }

    @Test
    void disableBatchingConfiguration() {
        WriteOptionsReactive writeOptions = WriteOptionsReactive.builder().batchSize(0).build();
        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(0);

        Assertions.assertThatThrownBy(() -> WriteOptionsReactive.builder().batchSize(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Expecting a positive or zero number for batchSize");
    }
}
