package org.influxdata.platform.impl;

import javax.annotation.Nonnull;

import org.influxdata.platform.WriteClientReactive;
import org.influxdata.platform.option.WriteOptions;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:50)
 */
public class WriteClientReactiveImpl extends AbstractWriteClient implements WriteClientReactive {


    private final PlatformReactiveService platformService;


    WriteClientReactiveImpl(@Nonnull final WriteOptions writeOptions,
                            @Nonnull final PlatformReactiveService platformService) {
        
        super(writeOptions, Schedulers.newThread(), Schedulers.computation(), Schedulers.trampoline(), Schedulers.trampoline());

        this.platformService = platformService;
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    Completable writeCall(final RequestBody requestBody,
                          final String organization,
                          final String bucket,
                          final String precision) {
        return platformService.writePoints(organization, bucket, precision, requestBody);
    }
}