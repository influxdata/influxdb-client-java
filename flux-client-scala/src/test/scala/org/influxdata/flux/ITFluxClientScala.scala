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

import org.influxdata.platform.error.InfluxException
import org.influxdata.platform.rest.LogLevel
import org.scalatest.Matchers

/**
 * @author Jakub Bednar (bednar@github) (06/11/2018 09:52)
 */
class ITFluxClientScala extends AbstractITFluxClientScala with Matchers {

  test("log level") {

    fluxClient.getLogLevel should be(LogLevel.NONE)

    fluxClient.setLogLevel(LogLevel.HEADERS)

    fluxClient.getLogLevel should be(LogLevel.HEADERS)
  }

  test("ping healthy") {

    fluxClient.ping should be(true)

  }

  test("ping not running server") {

    val clientNotRunning = FluxClientScalaFactory.create("http://localhost:8099")

    clientNotRunning.ping should be(false)

    clientNotRunning.close()
  }

  test("version") {

    fluxClient.version should not be empty
  }

  test("version not running server") {

    val clientNotRunning = FluxClientScalaFactory.create("http://localhost:8099")

    an [InfluxException] should be thrownBy clientNotRunning.version

    clientNotRunning.close()
  }
}
