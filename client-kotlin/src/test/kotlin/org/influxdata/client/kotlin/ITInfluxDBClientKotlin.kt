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
package org.influxdata.client.kotlin

import org.assertj.core.api.Assertions
import org.influxdata.LogLevel
import org.influxdata.client.domain.Check
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith


/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 09:19)
 */
@RunWith(JUnitPlatform::class)
internal class ITInfluxDBClientKotlin : AbstractITInfluxDBClientKotlin() {

    @Test
    fun queryClient() {

        Assertions.assertThat(influxDBClient.getQueryKotlinApi()).isNotNull
    }

    @Test
    fun health() {

        val health = influxDBClient.health()

        Assertions.assertThat(health).isNotNull
        Assertions.assertThat(health.status).isEqualTo(Check.StatusEnum.PASS)
        Assertions.assertThat(health.message).isEqualTo("ready for queries and writes")
    }

    @Test
    @Throws(Exception::class)
    fun healthNotRunningInstance() {

        val clientNotRunning = InfluxDBClientKotlinFactory.create("http://localhost:8099")

        val health = clientNotRunning.health()

        Assertions.assertThat(health).isNotNull
        Assertions.assertThat(health.status).isEqualTo(Check.StatusEnum.FAIL)
        Assertions.assertThat(health.message).startsWith("Failed to connect to")

        clientNotRunning.close()
    }

    @Test
    fun logLevel() {

        // default NONE
        Assertions.assertThat(influxDBClient.getLogLevel()).isEqualTo(LogLevel.NONE)

        // set HEADERS
        val influxDBClient = influxDBClient.setLogLevel(LogLevel.HEADERS)

        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient)
        Assertions.assertThat(this.influxDBClient.getLogLevel()).isEqualTo(LogLevel.HEADERS)
    }

    @Test
    fun gzip() {

        Assertions.assertThat(influxDBClient.isGzipEnabled()).isFalse()

        // Enable GZIP
        var influxDBClient = influxDBClient.enableGzip()
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient)
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isTrue()

        // Disable GZIP
        influxDBClient = this.influxDBClient.disableGzip()
        Assertions.assertThat(influxDBClient).isEqualTo(this.influxDBClient)
        Assertions.assertThat(this.influxDBClient.isGzipEnabled()).isFalse()
    }
}