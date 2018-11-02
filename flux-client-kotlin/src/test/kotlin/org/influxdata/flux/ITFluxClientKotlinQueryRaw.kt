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
import assertk.assertions.endsWith
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

/**
 * @author Jakub Bednar (bednar@github) (31/10/2018 10:22)
 */
@RunWith(JUnitPlatform::class)
internal class ITFluxClientKotlinQueryRaw : AbstractITFluxClientKotlin() {

    @Test
    fun `Map to String`() = runBlocking {

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))"
                + "\t|> sum()")

        val lines = fluxClient.queryRaw(flux).toList()
        assert(lines).hasSize(7)
        assert(lines[0]).isEqualTo("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long")
        assert(lines[1]).isEqualTo("#group,false,false,true,true,true,true,true,true,false")
        assert(lines[2]).isEqualTo("#default,_result,,,,,,,,")
        assert(lines[3]).isEqualTo(",result,table,_start,_stop,_field,_measurement,host,region,_value")
        assert(lines[4]).endsWith(",free,mem,A,west,21")
        assert(lines[5]).endsWith(",free,mem,B,west,42")
        assert(lines[6]).isEmpty()
    }

    @Test
    fun `Custom dialect`() = runBlocking {

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"free\"))"
                + "\t|> sum()")

        val dialect = JSONObject()
                .put("header", false)
                .toString()

        val lines = fluxClient.queryRaw(flux, dialect).toList()

        assert(lines).hasSize(6)
        assert(lines[0]).isEqualTo("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long")
        assert(lines[1]).isEqualTo("#group,false,false,true,true,true,true,true,true,false")
        assert(lines[2]).isEqualTo("#default,_result,,,,,,,,")
        assert(lines[3]).endsWith(",free,mem,A,west,21")
        assert(lines[4]).endsWith(",free,mem,B,west,42")
        assert(lines[5]).isEmpty()
    }

    @Test
    fun `Stop processing`() = runBlocking {

        prepareChunkRecords()

        val flux = (FROM_FLUX_DATABASE + "\n"
                + "\t|> filter(fn: (r) => r[\"_measurement\"] == \"chunked\")\n"
                + "\t|> range(start: 1970-01-01T00:00:00.000000000Z)\n"
                + "\t|> window(every: 10m)")

        val lines = fluxClient.queryRaw(flux).take(9_000).toList()

        assert(lines).hasSize(9_000)
        assert(lines[55]).isEqualTo(",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000052Z,52,free,chunked,A,west")
        assert(lines[555]).isEqualTo(",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000000552Z,552,free,chunked,A,west")
        assert(lines[5555]).isEqualTo(",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000005552Z,5552,free,chunked,A,west")
        assert(lines[8003]).isEqualTo(",,0,1970-01-01T00:00:00Z,1970-01-01T00:10:00Z,1970-01-01T00:00:00.000008Z,8000,free,chunked,A,west")
    }
}