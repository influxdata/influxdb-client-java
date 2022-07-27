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
package com.influxdb.client.kotlin

import com.influxdb.test.AbstractMockServerTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * @author Jakub Bednar (09/06/2020 07:11)
 */
@RunWith(JUnitPlatform::class)
class InfluxDBClientKotlinTest : AbstractMockServerTest() {

    private lateinit var client: InfluxDBClientKotlin

    @BeforeEach
    fun beforeEach() {
        client = InfluxDBClientKotlinFactory.create(startMockServer())
    }

    @AfterEach
    fun afterEach() {
        client.close()
    }

    @Test
    fun userAgent() {

        enqueuedResponse()

        val queryKotlinApi = client.getQueryKotlinApi()
        val flux = "from(bucket:\"my-bucket\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
                "|> sum()"

        queryKotlinApi.query(flux, "my-org")

        val request = mockServer.takeRequest(10L, TimeUnit.SECONDS)

        Assertions.assertThat(request?.getHeader("User-Agent")).startsWith("influxdb-client-kotlin/")
    }

    @Test
    fun createApiInstance() {
        Assertions.assertThat(client.getQueryKotlinApi()).isNotNull
        Assertions.assertThat(client.getWriteKotlinApi()).isNotNull
    }
}