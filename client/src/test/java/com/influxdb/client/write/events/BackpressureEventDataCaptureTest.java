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
package com.influxdb.client.write.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Fred Park (fredjoonpark@github)
 */
class BackpressureEventDataCaptureTest {

    @Test
    public void testBackpressureEventWithoutData() {
        BackpressureEvent event = new BackpressureEvent(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES);
        
        Assertions.assertThat(event.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES);
        Assertions.assertThat(event.getDroppedLineProtocol()).isEmpty();
    }

    @Test
    public void testBackpressureEventWithData() {
        List<String> testData = Arrays.asList(
                "measurement,tag=test1 value=1 1000000000",
                "measurement,tag=test2 value=2 2000000000"
        );
        
        BackpressureEvent event = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES, 
                testData
        );
        
        Assertions.assertThat(event.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES);
        Assertions.assertThat(event.getDroppedLineProtocol()).hasSize(2);
        Assertions.assertThat(event.getDroppedLineProtocol()).containsExactly(
                "measurement,tag=test1 value=1 1000000000",
                "measurement,tag=test2 value=2 2000000000"
        );
    }

    @Test
    public void testBackpressureEventWithNullData() {
        BackpressureEvent event = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.FAST_EMITTING, 
                null
        );
        
        Assertions.assertThat(event.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.FAST_EMITTING);
        Assertions.assertThat(event.getDroppedLineProtocol()).isEmpty();
    }

    @Test
    public void testBackpressureEventWithEmptyData() {
        BackpressureEvent event = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES, 
                Collections.emptyList()
        );
        
        Assertions.assertThat(event.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES);
        Assertions.assertThat(event.getDroppedLineProtocol()).isEmpty();
    }

    @Test
    public void testDroppedLineProtocolIsUnmodifiable() {
        List<String> testData = Arrays.asList(
                "measurement,tag=test1 value=1 1000000000",
                "measurement,tag=test2 value=2 2000000000"
        );
        
        BackpressureEvent event = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES, 
                testData
        );
        
        List<String> bufferedData = event.getDroppedLineProtocol();
        
        // Should throw UnsupportedOperationException when trying to modify
        Assertions.assertThatThrownBy(() -> bufferedData.add("new point"))
                .isInstanceOf(UnsupportedOperationException.class);
        
        Assertions.assertThatThrownBy(() -> bufferedData.clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testLogEventWithBufferedData() {
        List<String> testData = Arrays.asList(
                "measurement,tag=test1 value=1 1000000000",
                "measurement,tag=test2 value=2 2000000000",
                "measurement,tag=test3 value=3 3000000000"
        );
        
        BackpressureEvent event = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES, 
                testData
        );
        
        // Should not throw any exceptions
        Assertions.assertThatCode(event::logEvent).doesNotThrowAnyException();
        
        // Verify the event contains the expected data
        Assertions.assertThat(event.getDroppedLineProtocol()).hasSize(3);
    }

    @Test
    public void testLogEventWithoutBufferedData() {
        BackpressureEvent event = new BackpressureEvent(BackpressureEvent.BackpressureReason.FAST_EMITTING);
        
        // Should not throw any exceptions
        Assertions.assertThatCode(event::logEvent).doesNotThrowAnyException();
        
        // Verify the event has no buffered data
        Assertions.assertThat(event.getDroppedLineProtocol()).isEmpty();
    }

    @Test
    public void testBackpressureReasons() {
        // Test FAST_EMITTING reason
        BackpressureEvent fastEmittingEvent = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.FAST_EMITTING
        );
        Assertions.assertThat(fastEmittingEvent.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.FAST_EMITTING);
        
        // Test TOO_MUCH_BATCHES reason
        BackpressureEvent tooMuchBatchesEvent = new BackpressureEvent(
                BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES
        );
        Assertions.assertThat(tooMuchBatchesEvent.getReason()).isEqualTo(BackpressureEvent.BackpressureReason.TOO_MUCH_BATCHES);
    }
}