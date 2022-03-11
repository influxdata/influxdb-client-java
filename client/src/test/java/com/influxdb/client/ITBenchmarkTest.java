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

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;

import com.influxdb.client.benchmark.BenchmarkOptions;
import com.influxdb.client.benchmark.ClientBenchmark;
import com.influxdb.client.domain.Bucket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Client benchmark test
 */
@RunWith(JUnitPlatform.class)
class ITBenchmarkTest extends AbstractITClientTest {

    private BucketsApi bucketsApi;
    private Bucket bucket;

    @BeforeEach
    void setUp() {

        bucketsApi = influxDBClient.getBucketsApi();
        OrganizationsApi organizationsApi = influxDBClient.getOrganizationsApi();

        organizationsApi.findOrganizations()
            .stream()
            .filter(org -> org.getName().endsWith("-IT"))
            .forEach(organizationsApi::deleteOrganization);

        bucketsApi.findBuckets()
            .stream()
            .filter(bucket -> bucket.getName().endsWith("-IT"))
            .forEach(bucket -> {
                console("delete bucket: " + bucket.getName());
                bucketsApi.deleteBucket(bucket);
            });

        bucket = bucketsApi.createBucket("benchmark-IT", findMyOrg());

    }

    @Test
    @EnabledIfSystemProperty(named = "clientBenchmark", matches = "true")
    public void benchmarkBenchmark() throws InterruptedException {

        //warmup
        clientBenchmark(50, 20, 100, 1_000, 5_000, 10_000_000);
        clientBenchmark(500, 20, 100, 1_000, 5_000, 10_000_000);
        clientBenchmark(1_000, 20, 100, 1_000, 5_000, 10_000_000);
        clientBenchmark(1_500, 20, 100, 1_000, 5_000, 10_000_000);

    }

    public void clientBenchmark(int numberOfThreads,
                                int secondsCount,
                                int lineProtocolsCount,
                                int flushInterval,
                                int batchSize,
                                int maxBufferSize) throws InterruptedException {

        console(("\n--- BENCHMARK START ---"));
        BenchmarkOptions opts = new BenchmarkOptions(getInfluxDb2Url(), "my-token", "my-org", bucket.getName(),
            "sensor_" + System.currentTimeMillis(),
            numberOfThreads, secondsCount, lineProtocolsCount, batchSize, maxBufferSize, flushInterval);

        Instant startWrite = Instant.now();
        com.sun.management.OperatingSystemMXBean osMxBean =
            ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);

        long cpuTimeStart = osMxBean.getProcessCpuTime();
        ClientBenchmark benchmarkWriter = new ClientBenchmark(opts).start();
        Instant stopWrite = Instant.now();
        long cpuTimeWrite = osMxBean.getProcessCpuTime() - cpuTimeStart;

        double count = benchmarkWriter.verify();
        Instant stopVerify = Instant.now();
        long cpuTimeVerify = osMxBean.getProcessCpuTime() - cpuTimeWrite;

        Duration writeDuration = Duration.between(startWrite, stopWrite);
        Duration verifyDuration = Duration.between(stopWrite, stopVerify);

        double duration = (double) writeDuration.toMillis() / 1000;
        double rate = count / duration;
        console(String.format("--> total rate:           %s msg/s", rate));
        console(String.format("--> write duration:       %s", writeDuration));
        console(String.format("--> write cpu time:       %s", Duration.ofNanos(cpuTimeWrite)));
        console(String.format("--> verify duration:      %s", verifyDuration));
        console(String.format("--> verity cpu time:      %s", Duration.ofNanos(cpuTimeVerify)));
        console(String.format("--> total duration:       %s", Duration.between(startWrite, stopVerify)));
        console(("--- BENCHMARK FINISH ---"));

        int idealRate = lineProtocolsCount * numberOfThreads;
        //maximal rate Macbook Pro I9 is cca 80_0000 msg/sec
        if (idealRate < 70_000) {
            Assertions.assertThat(rate).isGreaterThan(idealRate * 0.8);
        }
    }

    private void console(Object msg) {
        System.out.println(msg);
    }

}


