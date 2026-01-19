package com.influxdb.client.internal.flowable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.util.ArrayListSupplier;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
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
    public void applyWithBufferSizeConsumer() {
        AtomicInteger lastReportedSize = new AtomicInteger(-1);

        // Create the transformer instance
        FlowableTransformer<Integer, List<Integer>> transformer =
                new FlowableBufferTimedFlushable<>(
                        Flowable.empty(),  // dummy source, will be replaced by apply()
                        PublishProcessor.create(),
                        1_000,
                        TimeUnit.SECONDS,
                        4,
                        new TestScheduler(),
                        ArrayListSupplier.asSupplier(),
                        lastReportedSize::set
                );

        // this calls apply()
        Flowable.just(1, 2, 3, 4, 5, 6, 7, 8)
                .compose(transformer)
                .test()
                .assertResult(
                        Arrays.asList(1, 2, 3, 4),
                        Arrays.asList(5, 6, 7, 8),
                        Collections.emptyList()
                );
    }

    @Test
    public void bufferSizeConsumerNotifiedOnAdd() {
        PublishProcessor<Boolean> flushPublisher = PublishProcessor.create();
        AtomicInteger lastReportedSize = new AtomicInteger(-1);

        List<List<Integer>> results = new ArrayList<>();

        PublishProcessor<Integer> publisher = PublishProcessor.create();
        Disposable subscription = publisher
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                flushPublisher,
                                1_000,
                                TimeUnit.SECONDS,
                                10,  // Large batch size so items accumulate
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier(),
                                lastReportedSize::set
                        ))
                .subscribe(results::add);

        // Add items and verify size is reported
        publisher.offer(1);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(1);

        publisher.offer(2);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(2);

        publisher.offer(3);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(3);

        subscription.dispose();
    }

    @Test
    public void bufferSizeConsumerNotifiedZeroOnFlush() {
        PublishProcessor<Boolean> flushPublisher = PublishProcessor.create();
        AtomicInteger lastReportedSize = new AtomicInteger(-1);

        List<List<Integer>> results = new ArrayList<>();

        PublishProcessor<Integer> publisher = PublishProcessor.create();
        Disposable subscription = publisher
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                flushPublisher,
                                1_000,
                                TimeUnit.SECONDS,
                                10,
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier(),
                                lastReportedSize::set
                        ))
                .subscribe(results::add);

        // Add items
        publisher.offer(1);
        publisher.offer(2);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(2);

        // Flush - should report 0
        flushPublisher.offer(true);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(0);

        subscription.dispose();
    }

    @Test
    public void bufferSizeConsumerNotifiedZeroOnBatchSizeReached() {
        PublishProcessor<Boolean> flushPublisher = PublishProcessor.create();
        AtomicInteger lastReportedSize = new AtomicInteger(-1);
        List<Integer> reportedSizes = new ArrayList<>();

        List<List<Integer>> results = new ArrayList<>();

        PublishProcessor<Integer> publisher = PublishProcessor.create();
        Disposable subscription = publisher
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                flushPublisher,
                                1_000,
                                TimeUnit.SECONDS,
                                3,  // Small batch size
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier(),
                                size -> {
                                    lastReportedSize.set(size);
                                    reportedSizes.add(size);
                                }
                        ))
                .subscribe(results::add);

        // Add items up to batch size
        publisher.offer(1);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(1);

        publisher.offer(2);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(2);

        // Third item reaches batch size - should flush and report 0
        publisher.offer(3);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(0);

        // Verify the batch was emitted
        Assertions.assertThat(results).hasSize(1);
        Assertions.assertThat(results.get(0)).isEqualTo(Arrays.asList(1, 2, 3));

        subscription.dispose();
    }

    @Test
    public void bufferSizeConsumerNullIsAllowed() {
        // Test that null consumer doesn't cause issues
        Flowable.just(1, 2, 3, 4)
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                PublishProcessor.create(),
                                1_000,
                                TimeUnit.SECONDS,
                                2,
                                new TestScheduler(),
                                ArrayListSupplier.asSupplier(),
                                null  // null consumer
                        ))
                .test()
                .assertResult(
                        Arrays.asList(1, 2),
                        Arrays.asList(3, 4),
                        Collections.emptyList()
                );
    }

    @Test
    public void bufferSizeConsumerNotifiedOnTimerFlush() {
        PublishProcessor<Boolean> flushPublisher = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        AtomicInteger lastReportedSize = new AtomicInteger(-1);

        List<List<Integer>> results = new ArrayList<>();

        PublishProcessor<Integer> publisher = PublishProcessor.create();
        Disposable subscription = publisher
                .compose(source ->
                        new FlowableBufferTimedFlushable<>(
                                source,
                                flushPublisher,
                                1,  // 1 second flush interval
                                TimeUnit.SECONDS,
                                100,  // Large batch size
                                scheduler,
                                ArrayListSupplier.asSupplier(),
                                lastReportedSize::set
                        ))
                .subscribe(results::add);

        // Add items
        publisher.offer(1);
        publisher.offer(2);
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(2);

        // Advance time by exactly 1 second to trigger single timer flush
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        // Should report 0 after timer flush
        Assertions.assertThat(lastReportedSize.get()).isEqualTo(0);

        // Verify exactly one batch was emitted with our items
        Assertions.assertThat(results).hasSize(1);
        Assertions.assertThat(results.get(0)).isEqualTo(Arrays.asList(1, 2));

        subscription.dispose();
    }
}
