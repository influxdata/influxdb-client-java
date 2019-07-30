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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.flowable.FlowableWindowTimed;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (04/02/2019 11:54)
 */
@Disabled
public class ClientTest {

    @Test
    void restCall() {

        Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
        Scheduler scheduler2 = Schedulers.from(Executors.newSingleThreadExecutor());

        PublishProcessor<Flowable<String>> flush = PublishProcessor.create();
        PublishProcessor<String> restProcessor = PublishProcessor.create();

        int count = 2;
        int delay = 100;

        Flowable<Flowable<String>> boundary = restProcessor.window(count)
                .mergeWith(flush)
                ;

// defer the subscription to the real boundary
        PublishProcessor<Flowable<String>> tempBoundary = PublishProcessor.create();

        restProcessor
                .observeOn(scheduler)
//                .onBackpressureBuffer(100, () -> System.out.println("Backpressure"))
                .window(tempBoundary)
                .concatMapSingle(Flowable::toList)
                .filter(it -> !it.isEmpty())
                .subscribe(strings -> System.out.println("emit REST call >>> " + strings));

// now subscribe to the actual boundary, this will make sure `restProcessor` has boundary as
// a second consumer.
        boundary.subscribe(tempBoundary);

        restProcessor.offer("1");
        restProcessor.offer("2");

        restProcessor.offer("3");
        restProcessor.offer("4");

        restProcessor.offer("5");
        restProcessor.offer("6");

        restProcessor.offer("7");
        restProcessor.offer("8");

        restProcessor.offer("9");
        flush.offer(Flowable.just("flush"));

        restProcessor.offer("10");

        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
        }

        System.out.println("onComplete");

        restProcessor.onComplete();
        flush.onComplete();
    }

    @Test
    void restCall2() throws InterruptedException {

        Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

        PublishProcessor<Flowable<String>> flush = PublishProcessor.create();
        PublishProcessor<String> restProcessor = PublishProcessor.create();

        int count = 2;
        int delay = 100;

        Flowable<String> backpressure = restProcessor
                .observeOn(scheduler)
                .onBackpressureBuffer(100, () -> System.out.println("Backpressure"))
//                .window(delay, TimeUnit.MILLISECONDS, scheduler, count, true)
                ;

        FlowableWindowTimed<String> stringFlowableWindowTimed = new FlowableWindowTimed<>(backpressure, delay, delay, TimeUnit.MILLISECONDS, scheduler, count, Flowable.bufferSize(), true);
//        stringFlowableWindowTimed.compose(FlowableTransformers.windowUntil(v -> "#".equals(v)));



        Disposable subscribe = stringFlowableWindowTimed

//                .compose(FlowableTransformers.windowUntil(v -> "#".equals(v)))
                .concatMapSingle(Flowable::toList)
                .filter(it -> !it.isEmpty())
                .subscribe(strings -> System.out.println("emit REST call >>> " + strings + " t: " + Thread.currentThread().getId()));

        restProcessor.offer("1");
        restProcessor.offer("2");

        restProcessor.offer("3");
        restProcessor.offer("4");

        restProcessor.offer("5");
        restProcessor.offer("6");

        restProcessor.offer("7");
        restProcessor.offer("8");

        restProcessor.offer("9");
        Thread.sleep(5_00);
        flush.offer(Flowable.just("flush"));

        restProcessor.offer("10");

            Thread.sleep(5_000);

        System.out.println("onComplete");

        restProcessor.onComplete();
        flush.onComplete();
    }

    @Test
    void ddd() {

        PublishProcessor<Integer> source = PublishProcessor.create();
        source
                .observeOn(Schedulers.computation())
                .subscribe(System.out::println, Throwable::printStackTrace);
        IntStream.range(0, 10_000).forEach(source::onNext);
    }
}