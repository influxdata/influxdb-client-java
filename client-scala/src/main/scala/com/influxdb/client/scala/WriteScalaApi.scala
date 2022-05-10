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
package com.influxdb.client.scala

import akka.Done
import akka.stream.scaladsl.Sink
import com.influxdb.client.domain.WritePrecision

import scala.concurrent.Future

/**
 * The Scala API to write time-series data into InfluxDB 2.x.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2022 09:48)
 */
trait WriteScalaApi {
  /**
   * Write Line Protocol record into specified bucket.
   *
   * @param precision Precision for the unix timestamps within the body line-protocol.
   *                  The [[com.influxdb.client.domain.WritePrecision.NS]] will be used as the precision if not specified.
   * @param bucket    Specifies the destination bucket for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *                  `bucket` if the `bucket` is not specified.
   * @param org       Specifies the destination organization for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *                  if the `org` is not specified.
   * @return the sink that accept the record specified in InfluxDB Line Protocol. The `record` is considered as one batch unit.
   */
  def writeRecord(precision: Option[WritePrecision] = None, bucket: Option[String] = None, org: Option[String] = None): Sink[String, Future[Done]]

  /**
   * Write Line Protocol records into specified bucket.
   *
   * @param precision Precision for the unix timestamps within the body line-protocol.
   *                  The [[com.influxdb.client.domain.WritePrecision.NS]] will be used as the precision if not specified.
   * @param bucket    Specifies the destination bucket for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *                  `bucket` if the `bucket` is not specified.
   * @param org       Specifies the destination organization for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *                  if the `org` is not specified.
   * @return the sink that accept the records specified in InfluxDB Line Protocol. The `records` are considered as one batch unit.
   */
  def writeRecords(precision: Option[WritePrecision] = None, bucket: Option[String] = None, org: Option[String] = None): Sink[Seq[String], Future[Done]]
}
