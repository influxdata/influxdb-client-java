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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;

import com.influxdb.client.write.events.EventListener;
import com.influxdb.exceptions.InfluxException;

import org.assertj.core.api.Assertions;
import org.junit.Assert;

/**
 * @author Jakub Bednar (bednar@github) (11/01/2019 12:26)
 */
public class WriteEventListener<T> implements EventListener<T> {

    CountDownLatch countDownLatch;
    List<T> values = new ArrayList<>();
    List<InfluxException> errors = new ArrayList<>();

    WriteEventListener() {
        countDownLatch = new CountDownLatch(1);
    }


    @Override
    public void onEvent(@Nonnull final T value) {

        Assertions.assertThat(value).isNotNull();

        values.add(value);

        countDownLatch.countDown();
    }

    T getValue() {
        return values.get(0);
    }

    T popValue()
    {
        T value = values.get(0);
        values.remove(0);
        return value;
    }

    WriteEventListener<T> awaitCount(@SuppressWarnings("SameParameterValue") final int count) {

        long start = System.currentTimeMillis();
        for (; ; ) {
            if (System.currentTimeMillis() - start >= 5_000) {
                Assert.fail("Time elapsed");
                break;
            }
            if (values.size() >= count) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }
}