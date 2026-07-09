package com.influxdb.client.internal.flowable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableBufferTimed;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableInternalHelper;
import io.reactivex.rxjava3.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava3.internal.subscribers.LambdaSubscriber;
import io.reactivex.rxjava3.internal.subscribers.QueueDrainSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.EmptySubscription;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.QueueDrainHelper;
import io.reactivex.rxjava3.subscribers.SerializedSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Buffered flowable which is able to flush the buffer by the count,
 * time and also by the request by associated {@code publisher}.
 *
 * @param <T> the upstream value type
 * @param <U> the output value type
 * @see FlowableBufferTimed
 */
public final class FlowableBufferTimedFlushable<T, U extends List<? super T>> extends Flowable<U> {

    final Publisher<T> source;
    final Publisher<Boolean> flusher;

    final long timespan;
    final long timeskip;
    final TimeUnit unit;
    final Scheduler scheduler;
    final Supplier<U> bufferSupplier;
    final int maxSize;
    final boolean restartTimerOnMaxSize;

    public FlowableBufferTimedFlushable(Publisher<T> source,
                                        Publisher<Boolean> flusher,
                                        long timespan,
                                        TimeUnit unit,
                                        int maxSize,
                                        Scheduler scheduler,
                                        Supplier<U> bufferSupplier) {
        this.source = source;
        this.flusher = flusher;
        this.timespan = timespan;
        this.timeskip = timespan;
        this.unit = unit;
        this.scheduler = scheduler;
        this.bufferSupplier = bufferSupplier;
        this.maxSize = maxSize;
        this.restartTimerOnMaxSize = true;
    }

    @Override
    protected void subscribeActual(@NonNull final Subscriber<? super U> subscriber) {
        Scheduler.Worker w = scheduler.createWorker();
        source.subscribe(new BufferExactBoundedSubscriber<>(
                new SerializedSubscriber<>(subscriber),
                bufferSupplier,
                timespan, unit, maxSize, restartTimerOnMaxSize, w, flusher
        ));
    }

    static final class BufferExactBoundedSubscriber<T, U extends Collection<? super T>>
            extends QueueDrainSubscriber<T, U, U> implements Subscription, Runnable, Disposable {
        final Supplier<U> bufferSupplier;
        final long timespan;
        final TimeUnit unit;
        final int maxSize;
        final boolean restartTimerOnMaxSize;
        final Worker w;

        final Publisher<Boolean> flusher;

        U buffer;

        Disposable timer;

        Subscription upstream;

        long producerIndex;

        long consumerIndex;

        BufferExactBoundedSubscriber(
                Subscriber<? super U> actual,
                Supplier<U> bufferSupplier,
                long timespan, TimeUnit unit, int maxSize,
                boolean restartOnMaxSize, Worker w, Publisher<Boolean> flusher) {
            super(actual, new MpscLinkedQueue<>());
            this.bufferSupplier = bufferSupplier;
            this.timespan = timespan;
            this.unit = unit;
            this.maxSize = maxSize;
            this.restartTimerOnMaxSize = restartOnMaxSize;
            this.w = w;
            this.flusher = flusher;
        }

        @Override
        public void onSubscribe(@NonNull Subscription s) {
            if (!SubscriptionHelper.validate(this.upstream, s)) {
                return;
            }
            this.upstream = s;

            U b;

            try {
                b = Objects.requireNonNull(bufferSupplier.get(), "The supplied buffer is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                w.dispose();
                s.cancel();
                EmptySubscription.error(e, downstream);
                return;
            }

            buffer = b;

            downstream.onSubscribe(this);

            timer = w.schedulePeriodically(this, timespan, timespan, unit);

            s.request(Long.MAX_VALUE);

            flusher.subscribe(new LambdaSubscriber<>(
                    ignore -> run(),
                    Functions.ON_ERROR_MISSING,
                    Functions.EMPTY_ACTION,
                    FlowableInternalHelper.RequestMax.INSTANCE));
        }

        @Override
        public void onNext(T t) {
            U b;
            synchronized (this) {
                b = buffer;
                if (b == null) {
                    return;
                }

                b.add(t);

                if (b.size() < maxSize) {
                    return;
                }

                buffer = null;
                producerIndex++;
            }

            if (restartTimerOnMaxSize) {
                timer.dispose();
            }

            fastPathOrderedEmitMax(b, false, this);

            try {
                b = Objects.requireNonNull(bufferSupplier.get(), "The supplied buffer is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                cancel();
                downstream.onError(e);
                return;
            }

            synchronized (this) {
                buffer = b;
                consumerIndex++;
            }
            if (restartTimerOnMaxSize) {
                timer = w.schedulePeriodically(this, timespan, timespan, unit);
            }
        }

        @Override
        public void onError(Throwable t) {
            synchronized (this) {
                buffer = null;
            }
            downstream.onError(t);
            w.dispose();
        }

        @Override
        public void onComplete() {
            U b;
            synchronized (this) {
                b = buffer;
                buffer = null;
            }

            if (b != null) {
                queue.offer(b);
                done = true;
                if (enter()) {
                    QueueDrainHelper.drainMaxLoop(queue, downstream, false, this, this);
                }
                w.dispose();
            }
        }

        @Override
        public boolean accept(Subscriber<? super U> a, U v) {
            a.onNext(v);
            return true;
        }

        @Override
        public void request(long n) {
            requested(n);
        }

        @Override
        public void cancel() {
            if (!cancelled) {
                cancelled = true;
                dispose();
            }
        }

        @Override
        public void dispose() {
            synchronized (this) {
                buffer = null;
            }
            upstream.cancel();
            w.dispose();
        }

        @Override
        public boolean isDisposed() {
            return w.isDisposed();
        }

        @Override
        public void run() {
            U next;

            try {
                next = Objects.requireNonNull(bufferSupplier.get(), "The supplied buffer is null");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                cancel();
                downstream.onError(e);
                return;
            }

            U current;

            synchronized (this) {
                current = buffer;
                if (current == null || producerIndex != consumerIndex) {
                    return;
                }
                buffer = next;
            }

            fastPathOrderedEmitMax(current, false, this);
        }
    }
}
