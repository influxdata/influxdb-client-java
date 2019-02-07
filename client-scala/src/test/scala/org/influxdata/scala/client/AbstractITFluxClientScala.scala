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
package org.influxdata.scala.client

import org.influxdata.test.client.AbstractTest
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * @author Jakub Bednar (bednar@github) (06/11/2018 09:34)
 */
abstract class AbstractITFluxClientScala extends FunSuite with BeforeAndAfter {

  val DATABASE_NAME = "flux_database_scala"

  var influxDBUtils: InfluxDBUtils = _

  var fluxClient: FluxClientScala = _

  before {
    influxDBUtils = new InfluxDBUtils {}

    fluxClient = FluxClientScalaFactory.create(influxDBUtils.getUrl)

    influxDBUtils.query(s"CREATE DATABASE $DATABASE_NAME", DATABASE_NAME)

    val lineProtocol = Seq("mem,host=A,region=west free=10i 10000000000",
      "mem,host=A,region=west free=11i 20000000000",
      "mem,host=B,region=west free=20i 10000000000",
      "mem,host=B,region=west free=22i 20000000000",
      "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
      "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
      "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
      .mkString("\n")

    influxDBUtils.write(lineProtocol, DATABASE_NAME)
  }

  after {
    fluxClient.close()

    influxDBUtils.query(s"DROP DATABASE $DATABASE_NAME", DATABASE_NAME)
  }

  class InfluxDBUtils extends AbstractTest {
    def getUrl: String = super.getInfluxDbUrl

    def query(query: String, databaseName: String): Unit = super.influxDBQuery(query, databaseName)

    def write(lineProtocol: String, databaseName: String): Unit = super.influxDBWrite(lineProtocol, databaseName)

    def prepareChunkRecords(): Unit = super.prepareChunkRecords(DATABASE_NAME)
  }
}
