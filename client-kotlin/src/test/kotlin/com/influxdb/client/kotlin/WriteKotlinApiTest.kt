/**
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
package com.influxdb.client.kotlin

import com.influxdb.client.domain.WriteConsistency
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.client.write.WriteParameters
import com.influxdb.exceptions.UnauthorizedException
import com.influxdb.test.AbstractMockServerTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.RecordedRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * @author Jakub Bednar (20/04/2021 13:58)
 */
@RunWith(JUnitPlatform::class)
class WriteKotlinApiTest : AbstractMockServerTest() {

    private lateinit var client: InfluxDBClientKotlin
    private lateinit var writeApi: WriteKotlinApi

    @BeforeEach
    fun beforeEach() {
        client = InfluxDBClientKotlinFactory.create(
            startMockServer(),
            "my-token".toCharArray(),
            "my-org",
            "my-bucket"
        )
        writeApi = client.getWriteKotlinApi()
    }

    @AfterEach
    fun afterEach() {
        client.close()
    }

    @Test
    fun queryParameters(): Unit = runBlocking {
        enqueuedResponse()

        writeApi.writeRecord("h2o_feet,location=coyote_creek water_level=1.0 1", WritePrecision.NS, "b1", "org1")

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        val url = request!!.requestUrl!!
        Assertions.assertThat(url.queryParameter("org")).isEqualTo("org1")
        Assertions.assertThat(url.queryParameter("bucket")).isEqualTo("b1")
        Assertions.assertThat(url.queryParameter("precision")).isEqualTo("ns")

    }

    @Test
    fun queryParametersFromOptions(): Unit = runBlocking {
        enqueuedResponse()

        writeApi.writeRecord("h2o_feet,location=coyote_creek water_level=1.0 1", WritePrecision.S)

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        val url = request!!.requestUrl!!
        Assertions.assertThat(url.queryParameter("org")).isEqualTo("my-org")
        Assertions.assertThat(url.queryParameter("bucket")).isEqualTo("my-bucket")
        Assertions.assertThat(url.queryParameter("precision")).isEqualTo("s")
    }

    @Test
    fun record(): Unit = runBlocking {
        enqueuedResponse()

        writeApi.writeRecord("h2o_feet,location=coyote_creek water_level=1.0 1", WritePrecision.S)

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        Assertions.assertThat(request?.body?.readUtf8()).isEqualTo("h2o_feet,location=coyote_creek water_level=1.0 1")
    }

    @Test
    fun point(): Unit = runBlocking {
        enqueuedResponse()

        val point = Point
            .measurement("h2o")
            .addField("level", 1)
            .time(1, WritePrecision.NS)

        writeApi.writePoint(point)

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        Assertions.assertThat(request?.body?.readUtf8()).isEqualTo("h2o level=1i 1")
    }

    @Test
    fun pointDifferentPrecision(): Unit = runBlocking {
        enqueuedResponse()
        enqueuedResponse()

        val point1 = Point
            .measurement("h2o")
            .addField("level", 1)
            .time(1, WritePrecision.NS)

        val point2 = Point
            .measurement("h2o")
            .addField("level", 2)
            .time(2, WritePrecision.S)

        writeApi.writePoints(listOf(point1, point2))

        var request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        Assertions.assertThat(request?.body?.readUtf8()).isEqualTo("h2o level=1i 1")
        Assertions.assertThat(request!!.requestUrl!!.queryParameter("precision")).isEqualTo("ns")

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        Assertions.assertThat(request?.body?.readUtf8()).isEqualTo("h2o level=2i 2")
        Assertions.assertThat(request!!.requestUrl!!.queryParameter("precision")).isEqualTo("s")
    }

    @Test
    fun measurement(): Unit = runBlocking {
        enqueuedResponse()

        val mem = ITQueryKotlinApi.Mem()
        mem.host = "192.168.1.100"
        mem.region = "europe"
        mem.free = 40
        mem.time = Instant.ofEpochSecond(10)

        writeApi.writeMeasurement(mem, WritePrecision.NS)

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        Assertions
            .assertThat(request?.body?.readUtf8())
            .isEqualTo("mem,host=192.168.1.100,region=europe _value=40i 10000000000")
    }

    @Test
    fun exception() {
        mockServer.enqueue(
            createErrorResponse(
                "token does not have sufficient permissions",
                true,
                401
            )
        )

        Assertions
            .assertThatThrownBy {
                runBlocking {
                    writeApi.writeRecord("h2o_feet,location=coyote_creek water_level=1.0 1", WritePrecision.S)
                }
            }.hasMessageStartingWith("token does not have sufficient permissions")
            .isInstanceOf(UnauthorizedException::class.java)
    }

    @Test
    fun batching(): Unit = runBlocking {

        enqueuedResponse()
        enqueuedResponse()
        enqueuedResponse()
        enqueuedResponse()
        enqueuedResponse()

        val lineProtocols = flow {
            for (i in 1..49) {
                emit("h2o,location=coyote_creek level=${i}.0 $i")
            }
        }

        lineProtocols
            .chunks(10)
            .collect { batch -> writeApi.writeRecords(batch, WritePrecision.S)}

        var request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        var body = request?.body?.readUtf8()
        Assertions.assertThat(body).startsWith("h2o,location=coyote_creek level=1.0 1")
        Assertions.assertThat(body).endsWith("h2o,location=coyote_creek level=10.0 10")

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        body = request?.body?.readUtf8()
        Assertions.assertThat(body).startsWith("h2o,location=coyote_creek level=11.0 11")
        Assertions.assertThat(body).endsWith("h2o,location=coyote_creek level=20.0 20")

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        body = request?.body?.readUtf8()
        Assertions.assertThat(body).startsWith("h2o,location=coyote_creek level=21.0 21")
        Assertions.assertThat(body).endsWith("h2o,location=coyote_creek level=30.0 30")

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        body = request?.body?.readUtf8()
        Assertions.assertThat(body).startsWith("h2o,location=coyote_creek level=31.0 31")
        Assertions.assertThat(body).endsWith("h2o,location=coyote_creek level=40.0 40")

        request = mockServer.takeRequest(10L, TimeUnit.SECONDS)
        body = request?.body?.readUtf8()
        Assertions.assertThat(body).startsWith("h2o,location=coyote_creek level=41.0 41")
        Assertions.assertThat(body).endsWith("h2o,location=coyote_creek level=49.0 49")

        Assertions.assertThat(mockServer.requestCount).isEqualTo(5)
    }

    @Test
    fun writeParameters(): Unit = runBlocking {
        val assertParameters = Runnable {
            val request: RecordedRequest = try {
                takeRequest()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            Assertions.assertThat(request.requestUrl).isNotNull
            Assertions.assertThat("s")
                .isEqualTo(request.requestUrl!!.queryParameter("precision"))
            Assertions.assertThat("c").isEqualTo(request.requestUrl!!.queryParameter("bucket"))
            Assertions.assertThat("d").isEqualTo(request.requestUrl!!.queryParameter("org"))
            Assertions.assertThat("quorum")
                .isEqualTo(request.requestUrl!!.queryParameter("consistency"))
        }

        val parameters = WriteParameters("c", "d", WritePrecision.S, WriteConsistency.QUORUM)

        // records
        val lineProtocols = flow {
            for (i in 1..49) {
                emit("h2o,location=coyote_creek level=${i}.0 $i")
            }
        }
        enqueuedResponse()
        writeApi.writeRecords(lineProtocols, parameters)
        assertParameters.run()

        // points
        val point = Point
            .measurement("h2o")
            .addField("level", 1)
            .time(1, WritePrecision.S)

        enqueuedResponse()
        writeApi.writePoints(listOf(point, point).asFlow(), parameters)
        assertParameters.run()

        // measurements
        val mem = ITQueryKotlinApi.Mem()
        mem.host = "192.168.1.100"
        mem.region = "europe"
        mem.free = 40
        mem.time = Instant.ofEpochSecond(10)

        enqueuedResponse()
        writeApi.writeMeasurements(listOf(mem, mem).asFlow(), parameters)
        assertParameters.run()
    }

    private suspend fun <T> Flow<T>.chunks(size: Int): Flow<List<T>> = flow {
        val chunk = ArrayList<T>(size)
        collect {
            chunk += it
            if (chunk.size >= size) {
                emit(chunk)
                chunk.clear()
            }
        }
        if (chunk.size > 0) {
            emit(chunk)
        }
    }
}