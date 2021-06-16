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

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.internal.AbstractInfluxDBClient
import com.influxdb.test.AbstractTest
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import retrofit2.Retrofit

/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 08:32)
 */
@RunWith(JUnitPlatform::class)
class InfluxDBClientKotlinFactoryTest : AbstractTest() {

    @Test
    fun connect() {
        val client = InfluxDBClientKotlinFactory.create("http://localhost:9999")

        AssertionsForClassTypes.assertThat(client).isNotNull
    }

    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun loadFromProperties() {

        val influxDBClient = InfluxDBClientKotlinFactory.create()

        val options = getDeclaredField<InfluxDBClientOptions>(influxDBClient, "options", AbstractInfluxDBClient::class.java)

        Assertions.assertThat(options.url).isEqualTo("http://localhost:9999/")
        Assertions.assertThat(options.org).isEqualTo("my-org")
        Assertions.assertThat(options.bucket).isEqualTo("my-bucket")
        Assertions.assertThat(options.token).isEqualTo("my-token".toCharArray())
        AssertionsForClassTypes.assertThat(options.logLevel).isEqualTo(LogLevel.BASIC)
        AssertionsForClassTypes.assertThat(influxDBClient.getLogLevel()).isEqualTo(LogLevel.BASIC)

        val retrofit = getDeclaredField<Retrofit>(influxDBClient, "retrofit", AbstractInfluxDBClient::class.java)
        val okHttpClient = retrofit.callFactory() as OkHttpClient

        Assertions.assertThat(okHttpClient.readTimeoutMillis).isEqualTo(5000)
        Assertions.assertThat(okHttpClient.writeTimeoutMillis).isEqualTo(60000)
        Assertions.assertThat(okHttpClient.connectTimeoutMillis).isEqualTo(5000)
    }

    @Test
    fun autoClosable() {
        val client = InfluxDBClientKotlinFactory.create()
        client.use {
            Assertions.assertThat(it).isNotNull
        }
    }
}