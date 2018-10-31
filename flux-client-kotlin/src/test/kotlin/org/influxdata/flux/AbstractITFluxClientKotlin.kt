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

import org.influxdata.flux.impl.FluxClientImpl
import org.influxdata.platform.AbstractTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.logging.Logger

/**
 * @author Jakub Bednar (bednar@github) (30/10/2018 09:13)
 */
internal abstract class AbstractITFluxClientKotlin : AbstractTest() {

    @Suppress("PrivatePropertyName")
    private val LOG = Logger.getLogger(FluxClientImpl::class.java.name)

    val DATABASE_NAME = "flux_database_kotlin"

    internal lateinit var fluxClient: FluxClientKotlin
    protected val FROM_FLUX_DATABASE = String
            .format("from(bucket:\"%s\")", DATABASE_NAME)

    @BeforeEach
    protected fun setUp() {

        fluxClient = FluxClientKotlinFactory.create(influxDbURL)

        influxDBQuery("CREATE DATABASE $DATABASE_NAME", DATABASE_NAME)

        val lineProtocol = arrayOf("mem,host=A,region=west free=10i 10000000000",
                "mem,host=A,region=west free=11i 20000000000",
                "mem,host=B,region=west free=20i 10000000000",
                "mem,host=B,region=west free=22i 20000000000",
                "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
                "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
                "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
                .joinToString("\n")

        influxDBWrite(lineProtocol, DATABASE_NAME)
    }

    @AfterEach
    protected fun after() {

        fluxClient.close()

        influxDBQuery("DROP DATABASE $DATABASE_NAME", DATABASE_NAME)
    }

    protected fun prepareChunkRecords() {

        LOG.info("Preparing data...")

        val points = mutableListOf<String>()

        for (i in 1..500000) {
            points.add("chunked,host=A,region=west free=${i}i $i")

            if (i % 100000 == 0) {
                influxDBWrite(points.joinToString("\n"), DATABASE_NAME)
                points.clear()
            }
        }

        LOG.info("prepared")
    }
}