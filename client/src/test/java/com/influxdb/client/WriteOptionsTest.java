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

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 10:35)
 */
class WriteOptionsTest {

    @Test
    void defaults() {

        WriteOptions writeOptions = WriteOptions.builder().build();

        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(1000);
        Assertions.assertThat(writeOptions.getBufferLimit()).isEqualTo(10000);
        Assertions.assertThat(writeOptions.getFlushInterval()).isEqualTo(1000);
        Assertions.assertThat(writeOptions.getJitterInterval()).isEqualTo(0);
        Assertions.assertThat(writeOptions.getRetryInterval()).isEqualTo(5_000);
        Assertions.assertThat(writeOptions.getMaxRetries()).isEqualTo(5);
        Assertions.assertThat(writeOptions.getMaxRetryTime()).isEqualTo(180_000);
        Assertions.assertThat(writeOptions.getMaxRetryDelay()).isEqualTo(125_000);
        Assertions.assertThat(writeOptions.getExponentialBase()).isEqualTo(2);
        Assertions.assertThat(writeOptions.getConcatMapPrefetch()).isEqualTo(2);
        Assertions.assertThat(writeOptions.getWriteScheduler()).isEqualTo(Schedulers.newThread());
        Assertions.assertThat(writeOptions.getBackpressureStrategy()).isEqualTo(BackpressureOverflowStrategy.DROP_OLDEST);
        Assertions.assertThat(writeOptions.getCaptureBackpressureData()).isFalse();
    }

    @Test
    void configure() {

        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(10_000)
                .bufferLimit(500)
                .flushInterval(500)
                .jitterInterval(1_000)
                .retryInterval(2_000)
                .maxRetries(5)
                .maxRetryDelay(250_123)
                .exponentialBase(2)
                .concatMapPrefetch(5)
                .writeScheduler(Schedulers.computation())
                .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
                .captureBackpressureData(true)
                .build();

        Assertions.assertThat(writeOptions.getBatchSize()).isEqualTo(10_000);
        Assertions.assertThat(writeOptions.getBufferLimit()).isEqualTo(500);
        Assertions.assertThat(writeOptions.getFlushInterval()).isEqualTo(500);
        Assertions.assertThat(writeOptions.getJitterInterval()).isEqualTo(1_000);
        Assertions.assertThat(writeOptions.getRetryInterval()).isEqualTo(2_000);
        Assertions.assertThat(writeOptions.getMaxRetries()).isEqualTo(5);
        Assertions.assertThat(writeOptions.getMaxRetryDelay()).isEqualTo(250_123);
        Assertions.assertThat(writeOptions.getExponentialBase()).isEqualTo(2);
        Assertions.assertThat(writeOptions.getConcatMapPrefetch()).isEqualTo(5);
        Assertions.assertThat(writeOptions.getWriteScheduler()).isEqualTo(Schedulers.computation());
        Assertions.assertThat(writeOptions.getBackpressureStrategy()).isEqualTo(BackpressureOverflowStrategy.ERROR);
        Assertions.assertThat(writeOptions.getCaptureBackpressureData()).isTrue();
    }

    @Test
    void batchSizeEdgeCases() {
        // Minimum valid batch size (1)
        WriteOptions minBatch = WriteOptions.builder().batchSize(1).build();
        Assertions.assertThat(minBatch.getBatchSize()).isEqualTo(1);

        // Zero batch size should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().batchSize(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batchSize");

        // Negative batch size should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().batchSize(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batchSize");

        // Very large batch size
        WriteOptions largeBatch = WriteOptions.builder().batchSize(Integer.MAX_VALUE).build();
        Assertions.assertThat(largeBatch.getBatchSize()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void bufferLimitEdgeCases() {
        // Minimum valid buffer limit (1)
        WriteOptions minBuffer = WriteOptions.builder().bufferLimit(1).build();
        Assertions.assertThat(minBuffer.getBufferLimit()).isEqualTo(1);

        // Zero buffer limit should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().bufferLimit(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bufferLimit");

        // Negative buffer limit should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().bufferLimit(-100).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bufferLimit");

        // Very large buffer limit
        WriteOptions largeBuffer = WriteOptions.builder().bufferLimit(Integer.MAX_VALUE).build();
        Assertions.assertThat(largeBuffer.getBufferLimit()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void flushIntervalEdgeCases() {
        // Minimum valid flush interval (1ms)
        WriteOptions minFlush = WriteOptions.builder().flushInterval(1).build();
        Assertions.assertThat(minFlush.getFlushInterval()).isEqualTo(1);

        // Zero flush interval should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().flushInterval(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("flushInterval");

        // Negative flush interval should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().flushInterval(-500).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("flushInterval");

        // Very large flush interval
        WriteOptions largeFlush = WriteOptions.builder().flushInterval(Integer.MAX_VALUE).build();
        Assertions.assertThat(largeFlush.getFlushInterval()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void jitterIntervalEdgeCases() {
        // Zero jitter interval is valid (no jitter)
        WriteOptions noJitter = WriteOptions.builder().jitterInterval(0).build();
        Assertions.assertThat(noJitter.getJitterInterval()).isEqualTo(0);

        // Negative jitter interval should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().jitterInterval(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jitterInterval");

        // Very large jitter interval
        WriteOptions largeJitter = WriteOptions.builder().jitterInterval(Integer.MAX_VALUE).build();
        Assertions.assertThat(largeJitter.getJitterInterval()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void retryIntervalEdgeCases() {
        // Minimum valid retry interval (1ms)
        WriteOptions minRetry = WriteOptions.builder().retryInterval(1).build();
        Assertions.assertThat(minRetry.getRetryInterval()).isEqualTo(1);

        // Zero retry interval should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().retryInterval(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retryInterval");

        // Negative retry interval should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().retryInterval(-1000).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retryInterval");
    }

    @Test
    void maxRetriesEdgeCases() {
        // Zero retries (no retry)
        WriteOptions noRetry = WriteOptions.builder().maxRetries(1).build();
        Assertions.assertThat(noRetry.getMaxRetries()).isEqualTo(1);

        // Negative retries should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().maxRetries(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetries");

        // Very large number of retries
        WriteOptions manyRetries = WriteOptions.builder().maxRetries(1000).build();
        Assertions.assertThat(manyRetries.getMaxRetries()).isEqualTo(1000);
    }

    @Test
    void maxRetryDelayEdgeCases() {
        // Minimum valid retry delay (1ms)
        WriteOptions minDelay = WriteOptions.builder().maxRetryDelay(1).build();
        Assertions.assertThat(minDelay.getMaxRetryDelay()).isEqualTo(1);

        // Zero retry delay should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().maxRetryDelay(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetryDelay");

        // Negative retry delay should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().maxRetryDelay(-5000).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetryDelay");
    }

    @Test
    void exponentialBaseEdgeCases() {
        // Minimum valid exponential base (2)
        WriteOptions minBase = WriteOptions.builder().exponentialBase(2).build();
        Assertions.assertThat(minBase.getExponentialBase()).isEqualTo(2);

        // Base of 1 should throw exception (no exponential growth)
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().exponentialBase(1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exponentialBase");

        // Base less than 2 should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().exponentialBase(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exponentialBase");

        // Negative base should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().exponentialBase(-2).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exponentialBase");

        // Large exponential base
        WriteOptions largeBase = WriteOptions.builder().exponentialBase(10).build();
        Assertions.assertThat(largeBase.getExponentialBase()).isEqualTo(10);
    }

    @Test
    void concatMapPrefetchEdgeCases() {
        // Minimum valid prefetch (1)
        WriteOptions minPrefetch = WriteOptions.builder().concatMapPrefetch(1).build();
        Assertions.assertThat(minPrefetch.getConcatMapPrefetch()).isEqualTo(1);

        // Negative prefetch should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().concatMapPrefetch(-5).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("concatMapPrefetch");

        // Very large prefetch
        WriteOptions largePrefetch = WriteOptions.builder().concatMapPrefetch(1000).build();
        Assertions.assertThat(largePrefetch.getConcatMapPrefetch()).isEqualTo(1000);
    }

    @Test
    void allBackpressureStrategies() {
        // Test all available backpressure strategies
        for (BackpressureOverflowStrategy strategy : BackpressureOverflowStrategy.values()) {
            WriteOptions options = WriteOptions.builder()
                    .backpressureStrategy(strategy)
                    .build();
            Assertions.assertThat(options.getBackpressureStrategy()).isEqualTo(strategy);
        }
    }

    @Test
    void bufferLimitSmallerThanBatchSize() {
        // Buffer limit can be smaller than batch size (valid configuration)
        WriteOptions options = WriteOptions.builder()
                .batchSize(1000)
                .bufferLimit(500)
                .build();

        Assertions.assertThat(options.getBatchSize()).isEqualTo(1000);
        Assertions.assertThat(options.getBufferLimit()).isEqualTo(500);
    }

    @Test
    void maxRetryTimeBoundaries() {
        // Test maxRetryTime edge cases
        WriteOptions minTime = WriteOptions.builder().maxRetryTime(1).build();
        Assertions.assertThat(minTime.getMaxRetryTime()).isEqualTo(1);

        // Zero max retry time should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().maxRetryTime(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetryTime");

        // Negative max retry time should throw exception
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().maxRetryTime(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetryTime");

        // Very large max retry time
        WriteOptions largeTime = WriteOptions.builder().maxRetryTime(Integer.MAX_VALUE).build();
        Assertions.assertThat(largeTime.getMaxRetryTime()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void captureBackpressureDataBothStates() {
        // Test both true and false states
        WriteOptions captureTrue = WriteOptions.builder().captureBackpressureData(true).build();
        Assertions.assertThat(captureTrue.getCaptureBackpressureData()).isTrue();

        WriteOptions captureFalse = WriteOptions.builder().captureBackpressureData(false).build();
        Assertions.assertThat(captureFalse.getCaptureBackpressureData()).isFalse();
    }

    @Test
    void multipleBuilderCalls() {
        // Test that builder can be reused and values overridden
        WriteOptions.Builder builder = WriteOptions.builder();
        
        builder.batchSize(100);
        builder.batchSize(200); // Override
        
        WriteOptions options = builder.build();
        Assertions.assertThat(options.getBatchSize()).isEqualTo(200);
    }

    @Test
    void concatMapPrefetchValidation() {
        // Test that concatMapPrefetch must be positive
        Assertions.assertThatThrownBy(() -> WriteOptions.builder().concatMapPrefetch(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("concatMapPrefetch");

        WriteOptions options10 = WriteOptions.builder().concatMapPrefetch(10).build();
        Assertions.assertThat(options10.getConcatMapPrefetch()).isEqualTo(10);
    }

    @Test
    void backpressureConfiguration() {
        // Test that backpressure options can be configured together
        WriteOptions options = WriteOptions.builder()
                .batchSize(100)
                .bufferLimit(500)
                .backpressureStrategy(BackpressureOverflowStrategy.DROP_LATEST)
                .captureBackpressureData(true)
                .build();

        Assertions.assertThat(options.getBatchSize()).isEqualTo(100);
        Assertions.assertThat(options.getBufferLimit()).isEqualTo(500);
        Assertions.assertThat(options.getBackpressureStrategy()).isEqualTo(BackpressureOverflowStrategy.DROP_LATEST);
        Assertions.assertThat(options.getCaptureBackpressureData()).isTrue();
    }
}