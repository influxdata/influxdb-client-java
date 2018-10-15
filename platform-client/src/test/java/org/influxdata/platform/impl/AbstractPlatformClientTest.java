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
package org.influxdata.platform.impl;

import java.util.Optional;
import javax.annotation.Nonnull;

import org.influxdata.platform.AbstractMockServerTest;
import org.influxdata.platform.PlatformClient;
import org.influxdata.platform.PlatformClientFactory;
import org.influxdata.platform.WriteClient;
import org.influxdata.platform.option.WriteOptions;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 12:29)
 */
public class AbstractPlatformClientTest extends AbstractMockServerTest {

    protected PlatformClient platformClient;

    @BeforeEach
    protected void setUp() {

        platformClient = PlatformClientFactory.create(startMockServer());
    }

    @Nonnull
    protected WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions,
                                            @Nonnull final Scheduler batchScheduler,
                                            @Nonnull final Scheduler jitterScheduler,
                                            @Nonnull final Scheduler retryScheduler) {

        Optional<Object> platformService = ReflectionUtils.readFieldValue(PlatformClientImpl.class, "platformService",
                (PlatformClientImpl) platformClient);

        if (!platformService.isPresent()) {
            Assertions.fail();
        }

        return new WriteClientImpl(writeOptions,
                (PlatformService) platformService.get(),
                Schedulers.trampoline(),
                batchScheduler,
                jitterScheduler,
                retryScheduler);
    }
}