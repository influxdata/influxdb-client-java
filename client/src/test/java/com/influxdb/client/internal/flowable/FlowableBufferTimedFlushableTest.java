package com.influxdb.client.internal.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.util.ArrayListSupplier;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2022 07:40)
 */
class FlowableBufferTimedFlushableTest {

    @Test
    public void bySize() {
        Flowable.just(1, 2, 3, 4, 5, 6, 7, 8)
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                PublishProcessor.create(),
                                1_000,
                                TimeUnit.SECONDS,
                                4,
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier()
                        ))
                .test()
                .assertResult(
                        Arrays.asList(1, 2, 3, 4),
                        Arrays.asList(5, 6, 7, 8),
                        Collections.emptyList()
                );
    }

    @Test
    public void byFlusher() {
        PublishProcessor<Boolean> flushPublisher = PublishProcessor.create();

        List<List<Integer>> results = new ArrayList<>();

        PublishProcessor<Integer> publisher = PublishProcessor.create();
        Disposable subscription = publisher
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                flushPublisher,
                                1_000,
                                TimeUnit.SECONDS,
                                4,
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier()
                        ))
                .subscribe(results::add);

        Assertions.assertThat(results).isEmpty();

        publisher.offer(1);
        publisher.offer(2);
        publisher.offer(3);

        Assertions.assertThat(results).isEmpty();

        flushPublisher.offer(true);

        Assertions.assertThat(results).hasSize(1);
        Assertions.assertThat(results.get(0)).isEqualTo(Arrays.asList(1, 2, 3));

        subscription.dispose();
    }

    @Test
    public void byTimerNotInterruptedException() throws InterruptedException {
        PublishProcessor<Integer> publisher = PublishProcessor.create();
        AtomicBoolean hasError = new AtomicBoolean(false);
        CountDownLatch firstConsume = new CountDownLatch(1);
        CountDownLatch consumeTwice = new CountDownLatch(2);

        Consumer<List<Integer>> listConsumer = (List<Integer> a) -> {

            try {
                firstConsume.countDown();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                hasError.set(true);
            } finally {
                consumeTwice.countDown();
            }
        };
        Disposable subscribe = publisher
            .compose(source ->
                         new FlowableBufferTimedFlushable<>(
                             source,
                             PublishProcessor.create(),
                             1,
                             TimeUnit.SECONDS,
                             4,
                             Schedulers.newThread(),
                             ArrayListSupplier.asSupplier()
                         )).subscribe(listConsumer);

        publisher.offer(1);
        publisher.offer(2);
        publisher.offer(3);

        firstConsume.await();
        publisher.offer(4);
        publisher.offer(5);
        publisher.offer(6);
        publisher.offer(7);

        consumeTwice.countDown();
        Assertions.assertThat(hasError.get()).isFalse();
        subscribe.dispose();
    }

}
