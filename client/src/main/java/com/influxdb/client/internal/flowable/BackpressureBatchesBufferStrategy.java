package com.influxdb.client.internal.flowable;

import java.util.Collections;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.influxdb.client.internal.AbstractWriteClient;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.FlowableOperator;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableOnBackpressureBufferStrategy;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * The backpressure strategy which uses total sum of {@link AbstractWriteClient.BatchWriteItem#length()}
 * to determine backpressure boundary.
 * <p>
 * The original strategy {@link FlowableOnBackpressureBufferStrategy} uses only count of elements.
 *
 * @see FlowableOnBackpressureBufferStrategy
 */
public final class BackpressureBatchesBufferStrategy implements
        FlowableOperator<AbstractWriteClient.BatchWriteItem, AbstractWriteClient.BatchWriteItem> {

    final long bufferSize;

    final Consumer<List<String>> onOverflow;

    final BackpressureOverflowStrategy strategy;

    final boolean captureBackpressureData;

    public BackpressureBatchesBufferStrategy(long bufferSize,
            Consumer<List<String>> onOverflow,
            BackpressureOverflowStrategy strategy) {
        this(bufferSize, onOverflow, strategy, false);
    }

    public BackpressureBatchesBufferStrategy(long bufferSize,
            Consumer<List<String>> onOverflow,
            BackpressureOverflowStrategy strategy,
            boolean captureBackpressureData) {
        this.bufferSize = bufferSize;
        this.onOverflow = onOverflow;
        this.strategy = strategy;
        this.captureBackpressureData = captureBackpressureData;
    }

    @Override
    public @NonNull Subscriber<? super AbstractWriteClient.BatchWriteItem> apply(
            @NonNull final Subscriber<? super AbstractWriteClient.BatchWriteItem> subscriber) throws Throwable {
        return new OnBackpressureBufferStrategySubscriber(subscriber, onOverflow, strategy, bufferSize, captureBackpressureData);
    }

    static final class OnBackpressureBufferStrategySubscriber
            extends AtomicInteger
            implements FlowableSubscriber<AbstractWriteClient.BatchWriteItem>, Subscription {

        private static final long serialVersionUID = 3240706908776709697L;

        final Subscriber<? super AbstractWriteClient.BatchWriteItem> downstream;

        final BackpressureOverflowStrategy strategy;

        final long bufferSize;

        final AtomicLong requested;

        final Deque<AbstractWriteClient.BatchWriteItem> deque;

        Subscription upstream;

        volatile boolean cancelled;

        volatile boolean done;
        Throwable error;

        final Consumer<List<String>> onOverflow;

        final boolean captureBackpressureData;

        OnBackpressureBufferStrategySubscriber(Subscriber<? super AbstractWriteClient.BatchWriteItem> actual,
                                               Consumer<List<String>> onOverflow,
                                               BackpressureOverflowStrategy strategy,
                                               long bufferSize,
                                               boolean captureBackpressureData) {
            this.downstream = actual;
            this.onOverflow = onOverflow;
            this.strategy = strategy;
            this.bufferSize = bufferSize;
            this.captureBackpressureData = captureBackpressureData;
            this.requested = new AtomicLong();
            this.deque = new ArrayDeque<>();
        }

        @Override
        public void onSubscribe(@NonNull Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;

                downstream.onSubscribe(this);

                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(AbstractWriteClient.BatchWriteItem t) {
            if (done) {
                return;
            }
            boolean callOnOverflow = false;
            boolean callError = false;
            Deque<AbstractWriteClient.BatchWriteItem> dq = deque;
            List<String> overflowSnapshot = null;
            synchronized (dq) {
                AtomicLong size = new AtomicLong(t.length());
                dq.forEach(batchWriteItem -> size.addAndGet(batchWriteItem.length()));
                if (size.get() > bufferSize) {
                    switch (strategy) {
                        case DROP_LATEST:
                            if (captureBackpressureData) {
                                overflowSnapshot = captureBatch(t);
                            }
                            dq.pollLast();
                            dq.offer(t);
                            callOnOverflow = true;
                            break;
                        case DROP_OLDEST:
                            AbstractWriteClient.BatchWriteItem droppedBatch = dq.poll();
                            if (captureBackpressureData) {
                                overflowSnapshot = captureBatch(droppedBatch);
                            }
                            dq.offer(t);
                            callOnOverflow = true;
                            break;
                        default:
                            // signal error
                            callError = true;
                            break;
                    }
                } else {
                    dq.offer(t);
                }
            }

            if (callOnOverflow) {
                if (onOverflow != null) {
                    try {
                        List<String> droppedPoints;
                        if (captureBackpressureData) {
                            droppedPoints = overflowSnapshot;
                        } else {
                            droppedPoints = Collections.emptyList();
                        }
                        onOverflow.accept(droppedPoints);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.cancel();
                        onError(ex);
                    }
                }
            } else if (callError) {
                upstream.cancel();
                onError(new MissingBackpressureException());
            } else {
                drain();
            }
        }

        /**
         * Captures snapshot of a single batch item for overflow handling.
         *
         * @param item the batch item to capture
         * @return list of line protocol points from the item
         */
        List<String> captureBatch(AbstractWriteClient.BatchWriteItem item) {
            String lp = item.toLineProtocol();
            if (lp == null || lp.isEmpty()) {
                return Collections.emptyList();
            }
            
            return Arrays.stream(lp.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            done = true;
            drain();
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                drain();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();

            if (getAndIncrement() == 0) {
                clear(deque);
            }
        }

        void clear(Deque<AbstractWriteClient.BatchWriteItem> dq) {
            synchronized (dq) {
                dq.clear();
            }
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Deque<AbstractWriteClient.BatchWriteItem> dq = deque;
            Subscriber<? super AbstractWriteClient.BatchWriteItem> a = downstream;
            for (; ; ) {
                long r = requested.get();
                long e = 0L;
                while (e != r) {
                    if (cancelled) {
                        clear(dq);
                        return;
                    }

                    boolean d = done;

                    AbstractWriteClient.BatchWriteItem v;

                    synchronized (dq) {
                        v = dq.poll();
                    }

                    boolean empty = v == null;

                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            clear(dq);
                            a.onError(ex);
                            return;
                        }
                        if (empty) {
                            a.onComplete();
                            return;
                        }
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(v);

                    e++;
                }

                if (e == r) {
                    if (cancelled) {
                        clear(dq);
                        return;
                    }

                    boolean d = done;

                    boolean empty;

                    synchronized (dq) {
                        empty = dq.isEmpty();
                    }

                    if (d) {
                        Throwable ex = error;
                        if (ex != null) {
                            clear(dq);
                            a.onError(ex);
                            return;
                        }
                        if (empty) {
                            a.onComplete();
                            return;
                        }
                    }
                }

                if (e != 0L) {
                    BackpressureHelper.produced(requested, e);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }
}
