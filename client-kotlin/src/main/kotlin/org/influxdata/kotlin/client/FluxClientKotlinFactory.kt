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
package org.influxdata.kotlin.client

import org.influxdata.client.Arguments
import org.influxdata.flux.client.FluxConnectionOptions
import org.influxdata.kotlin.client.internal.FluxApiKotlinImpl

/**
 * The Factory that creates a instance of a Flux client.
 * 
 * @author Jakub Bednar (bednar@github) (30/10/2018 08:19)
 */
class FluxClientKotlinFactory {

    companion object {

        /**
         * Create a instance of the Flux client.
         *
         * @param connectionString the connectionString to connect to InfluxDB.
         * @return client
         * @see FluxConnectionOptions#Builder#builder(String)
         */
        fun create(connectionString: String): FluxClientKotlin {

            Arguments.checkNonEmpty(connectionString, "connectionString")

            val options = FluxConnectionOptions
                    .builder(connectionString)
                    .build()

            return create(options)
        }

        /**
         * Create a instance of the Flux client.
         *
         * @param options the connection configuration
         * @return client
         * @see FluxConnectionOptions#Builder#builder(String)
         */
        fun create(options: FluxConnectionOptions): FluxClientKotlin {

            Arguments.checkNotNull(options, "FluxConnectionOptions")
            
            return FluxApiKotlinImpl(options)
        }
    }
}
