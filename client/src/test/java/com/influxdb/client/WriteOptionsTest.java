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
package com.influxdb.client;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.schedulers.Schedulers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 10:35)
 */
@RunWith(JUnitPlatform.class)
class WriteOptionsTest {

    @Test
    void defaults() {

        WriteOptions writeOptions = WriteOptions.builder().build();

        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(1000);
        Assertions.assertThat(writeOptions.getBufferLimit()).isEqualTo(10000);
        Assertions.assertThat(writeOptions.getFlushInterval()).isEqualTo(1000);
        Assertions.assertThat(writeOptions.getJitterInterval()).isEqualTo(0);
        Assertions.assertThat(writeOptions.getWriteScheduler()).isEqualTo(Schedulers.newThread());
        Assertions.assertThat(writeOptions.getBackpressureStrategy()).isEqualTo(BackpressureOverflowStrategy.DROP_OLDEST);
    }

    @Test
    void configure() {

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(10_000)
                .bufferLimit(500)
                .flushInterval(500)
                .jitterInterval(1_000)
                .retryInterval(2_000)
                .writeScheduler(Schedulers.computation())
                .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
                .build();

        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(10_000);
        Assertions.assertThat(writeOptions.getBufferLimit()).isEqualTo(500);
        Assertions.assertThat(writeOptions.getFlushInterval()).isEqualTo(500);
        Assertions.assertThat(writeOptions.getJitterInterval()).isEqualTo(1_000);
        Assertions.assertThat(writeOptions.getRetryInterval()).isEqualTo(2_000);
        Assertions.assertThat(writeOptions.getWriteScheduler()).isEqualTo(Schedulers.computation());
        Assertions.assertThat(writeOptions.getBackpressureStrategy()).isEqualTo(BackpressureOverflowStrategy.ERROR);
    }
}