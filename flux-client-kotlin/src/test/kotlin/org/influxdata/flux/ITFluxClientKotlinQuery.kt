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
package org.influxdata.flux

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.influxdata.platform.annotations.Column
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.net.ConnectException
import java.time.Instant

/**
 * @author Jakub Bednar (bednar@github) (31/10/2018 07:16)
 */
@RunWith(JUnitPlatform::class)
internal class ITFluxClientKotlinQuery : AbstractITFluxClientKotlin() {

    @Test
    fun `Simple query mapped to FluxRecords`() = runBlocking {

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))\n"
                + "\t|> sum()")

        val records = fluxClient.query(flux)

        val tables = records.toList()
        assert(tables).hasSize(2)
    }

    @Test
    fun `Simple query FluxRecords order`() = runBlocking {

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)")

        val records = fluxClient.query(flux)

        val values = records.map { it.value }.toList()

        assert(values).hasSize(10)
        assert(values).containsExactly(55L, 65L, 35L, 38L, 45L, 49L, 10L, 11L, 20L, 22L)
    }

    @Test
    fun `Mapping to POJO`() = runBlocking {

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))")

        val query = fluxClient.query(flux, Mem::class.java)
        val memory = query.toList()

        assert(memory).hasSize(4)

        assert(memory[0].host).isEqualTo("A")
        assert(memory[0].region).isEqualTo("west")
        assert(memory[0].free).isEqualTo(10L)
        assert(memory[0].time).isEqualTo(Instant.ofEpochSecond(10))

        assert(memory[1].host).isEqualTo("A")
        assert(memory[1].region).isEqualTo("west")
        assert(memory[1].free).isEqualTo(11L)
        assert(memory[1].time).isEqualTo(Instant.ofEpochSecond(20))

        assert(memory[2].host).isEqualTo("B")
        assert(memory[2].region).isEqualTo("west")
        assert(memory[2].free).isEqualTo(20L)
        assert(memory[2].time).isEqualTo(Instant.ofEpochSecond(10))

        assert(memory[3].host).isEqualTo("B")
        assert(memory[3].region).isEqualTo("west")
        assert(memory[3].free).isEqualTo(22L)
        assert(memory[3].time).isEqualTo(Instant.ofEpochSecond(20))
    }

    @Test
    fun `Stop processing`() = runBlocking {

        prepareChunkRecords()

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> window(every: 10m)")

        val records = fluxClient.query(flux).take(9_000).toList()

        assert(records).hasSize(9_000)
    }

    @Test
    fun `Not running server`() {

        val clientNotRunning = FluxClientKotlinFactory.create("http://localhost:8099")

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)")

        val channel = clientNotRunning.query(flux)

        assert { runBlocking { channel.toList() } }
                .thrownError { isInstanceOf(ConnectException::class.java) }

        assert(channel.isClosedForReceive).isTrue()
        assert(channel.isClosedForSend).isTrue()

        clientNotRunning.close()
    }

    class Mem {

        internal val host: String? = null
        internal val region: String? = null

        @Column(name = "_value")
        internal val free: Long? = null
        @Column(name = "_time")
        internal val time: Instant? = null
    }
}