package com.influxdb.client.internal.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.internal.AbstractWriteClient;
import com.influxdb.client.write.WriteParameters;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.reactivex.rxjava3.core.BackpressureOverflowStrategy.DROP_OLDEST;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2022 07:54)
 */
class BackpressureBatchesBufferStrategyTest {

    @Test
    public void backpressureCountContent() {
        int dataPerBatches = 5;
        int bufferSize = 20;
        AtomicInteger backpressure = new AtomicInteger(0);

        TestSubscriber<AbstractWriteClient.BatchWriteItem> testSubscriber = new TestSubscriber<>(new DefaultSubscriber<AbstractWriteClient.BatchWriteItem>() {

            @Override
            protected void onStart() {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(AbstractWriteClient.BatchWriteItem t) {
            }

        }, 0L);

        Flowable.fromPublisher(
                        itemsToWrite(dataPerBatches)
                                .lift(new BackpressureBatchesBufferStrategy(
                                        bufferSize,
                                        droppedPoints -> backpressure.incrementAndGet(),
                                        DROP_OLDEST)))
                .subscribe(testSubscriber);

        // request over buffer size
        testSubscriber.request(10 * 5);
        testSubscriber
                .awaitDone(5, TimeUnit.SECONDS)
                .assertNoErrors();

        Assertions.assertThat(bufferSize / dataPerBatches).isEqualTo(testSubscriber.values().size());
        Assertions.assertThat(backpressure.get()).isEqualTo(100 - (bufferSize / dataPerBatches));
        Assertions.assertThat(testSubscriber.values().get(0).toLineProtocol()).endsWith("m2m,tag=5 value=5 97");
        Assertions.assertThat(testSubscriber.values().get(1).toLineProtocol()).endsWith("m2m,tag=5 value=5 98");
        Assertions.assertThat(testSubscriber.values().get(2).toLineProtocol()).endsWith("m2m,tag=5 value=5 99");
        Assertions.assertThat(testSubscriber.values().get(3).toLineProtocol()).endsWith("m2m,tag=5 value=5 100");
    }

    //
    // Create 100 * countOfDataInBatch items
    //
    private Flowable<AbstractWriteClient.BatchWriteItem> itemsToWrite(int countOfDataInBatch) {
        return Flowable.unsafeCreate(s -> {
            BooleanSubscription bs = new BooleanSubscription();
            s.onSubscribe(bs);
            long count = 1;
            while (!bs.isCancelled() && count <= 100) {

                WriteParameters writeParameters = new WriteParameters("my-bucket", "my-org", WritePrecision.NS);
                AbstractWriteClient.BatchWriteDataGrouped data = new AbstractWriteClient.BatchWriteDataGrouped(writeParameters);
                for (int i = 1; i <= countOfDataInBatch; i++) {

                    data.append(String.format("m2m,tag=%d value=%d %d", i, i, count));
                }
                count++;
                s.onNext(new AbstractWriteClient.BatchWriteItem(writeParameters, data));
            }
            if (!bs.isCancelled()) {
                s.onComplete();
            }
        });
    }
}
