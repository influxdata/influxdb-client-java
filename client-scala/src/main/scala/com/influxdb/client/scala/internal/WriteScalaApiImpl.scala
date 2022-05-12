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
package com.influxdb.client.scala.internal

import akka.Done
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.internal.{AbstractWriteBlockingClient, AbstractWriteClient}
import com.influxdb.client.scala.WriteScalaApi
import com.influxdb.client.service.WriteService
import com.influxdb.client.write.{Point, WriteParameters}

import javax.annotation.Nonnull
import scala.collection.immutable.ListMap
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

/**
 * @author Jakub Bednar (bednar@github) (05/09/2022 09:48)
 */
class WriteScalaApiImpl(@Nonnull service: WriteService, @Nonnull options: InfluxDBClientOptions)

  extends AbstractWriteBlockingClient(service, options) with WriteScalaApi {

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
  override def writeRecord(precision: Option[WritePrecision], bucket: Option[String], org: Option[String]): Sink[String, Future[Done]] = {
    Flow[String]
      .map(record => Seq(new AbstractWriteClient.BatchWriteDataRecord(record)))
      .map(batch => writeHttp(precision, bucket, org, batch))
      .toMat(Sink.head)(Keep.right)
  }

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
   * @return the sink that accept the records specified in InfluxDB Line Protocol.
   */
  override def writeRecords(precision: Option[WritePrecision], bucket: Option[String], org: Option[String]): Sink[Seq[String], Future[Done]] = {
    writeRecords(toWriteParameters(precision, bucket, org))
  }

  /**
   * Write Line Protocol records into specified bucket.
   *
   * @param parameters specify InfluxDB Write endpoint parameters
   * @return the sink that accept the records specified in InfluxDB Line Protocol. The `records` are considered as one batch unit.
   */
  override def writeRecords(parameters: WriteParameters): Sink[Seq[String], Future[Done]] = {
    Flow[Seq[String]]
      .map(records => records.map(record => new AbstractWriteClient.BatchWriteDataRecord(record)))
      .map(batch => writeHttp(parameters, batch))
      .toMat(Sink.head)(Keep.right)
  }

  /**
   * Write Data points into specified bucket.
   *
   * @param bucket Specifies the destination bucket for writes.
   *               The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *               `bucket` if the `bucket` is not specified.
   * @param org    Specifies the destination organization for writes.
   *               The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *               if the `org` is not specified.
   * @return the sink that accept the Data points. The `point` is considered as one batch unit.
   */
  override def writePoint(bucket: Option[String], org: Option[String]): Sink[Point, Future[Done]] = {
    Flow[Point]
      .map(point => (point.getPrecision, Seq(new AbstractWriteClient.BatchWriteDataPoint(point, options))))
      .map(batch => writeHttp(Some(batch._1), bucket, org, batch._2))
      .toMat(Sink.head)(Keep.right)
  }

  /**
   * Write Data points into specified bucket.
   *
   * @param bucket Specifies the destination bucket for writes.
   *               The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *               `bucket` if the `bucket` is not specified.
   * @param org    Specifies the destination organization for writes.
   *               The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *               if the `org` is not specified.
   * @return the sink that accept the Data points. The `points` are considered as one batch unit.
   */
  override def writePoints(bucket: Option[String], org: Option[String]): Sink[Seq[Point], Future[Done]] = {
    writePoints(new WriteParameters(bucket.orNull, org.orNull, null, null))
  }

  /**
   * Write Data points into specified bucket.
   *
   * @param parameters specify InfluxDB Write endpoint parameters
   * @return the sink that accept the Data points. The `points` are considered as one batch unit.
   */
  override def writePoints(parameters: WriteParameters): Sink[Seq[Point], Future[Done]] = {
    Flow[Seq[Point]]
      // create ordered Map<Precision, Seq[Point]>
      .map(records => records.foldRight(ListMap.empty[WritePrecision, Seq[Point]]) {
        case (point, map) => map.updated(point.getPrecision, point +: map.getOrElse(point.getPrecision, Seq()))
      }.toList.reverse)
      .map(grouped => grouped.map(group => (group._1, group._2.map(point => new AbstractWriteClient.BatchWriteDataPoint(point, options)))))
      .map(batches => batches.foreach(batch => writeHttp(parameters.copy(batch._1, options), batch._2)))
      .map(_ => Done.done())
      .toMat(Sink.head)(Keep.right)
  }

  /**
   * Write Measurement into specified bucket.
   *
   * @param precision Precision for the unix timestamps within the body line-protocol.
   *                  The [[com.influxdb.client.domain.WritePrecision.NS]] will be used as the precision if not specified.
   * @param bucket    Specifies the destination bucket for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *                  `bucket` if the `bucket` is not specified.
   * @param org       Specifies the destination organization for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *                  if the `org` is not specified.
   * @tparam M the type of the measurement (POJO)
   * @return the sink that accept the measurement. The `measurement` is considered as one batch unit.
   */
  override def writeMeasurement[M](precision: Option[WritePrecision], bucket: Option[String], org: Option[String]): Sink[M, Future[Done]] = {
    Flow[M]
      .map(measurement => {
        val parameters = toWriteParameters(precision, bucket, org)
        Seq(toMeasurementBatch(measurement, parameters.precisionSafe(options)))
      })
      .map(batch => writeHttp(precision, bucket, org, batch))
      .toMat(Sink.head)(Keep.right)
  }

  /**
   * Write Measurements into specified bucket.
   *
   * @param precision Precision for the unix timestamps within the body line-protocol.
   *                  The [[com.influxdb.client.domain.WritePrecision.NS]] will be used as the precision if not specified.
   * @param bucket    Specifies the destination bucket for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getBucket]] will be used as the destination
   *                  `bucket` if the `bucket` is not specified.
   * @param org       Specifies the destination organization for writes.
   *                  The [[com.influxdb.client.InfluxDBClientOptions#getOrg]] will be used as the destination `organization`
   *                  if the `org` is not specified.
   * @tparam M the type of the measurement (POJO)
   * @return the sink that accept the measurements. The `measurements` are considered as one batch unit.
   */
  override def writeMeasurements[M](precision: Option[WritePrecision], bucket: Option[String], org: Option[String]): Sink[Seq[M], Future[Done]] = {
    writeMeasurements(toWriteParameters(precision, bucket, org))
  }

  /**
   * Write Measurements into specified bucket.
   *
   * @param parameters specify InfluxDB Write endpoint parameters
   * @tparam M the type of the measurement (POJO)
   * @return the sink that accept the measurements. The `measurements` are considered as one batch unit.
   */
  override def writeMeasurements[M](parameters: WriteParameters): Sink[Seq[M], Future[Done]] = {
    Flow[Seq[M]]
      .map(records => records.map(record => toMeasurementBatch(record, parameters.precisionSafe(options))))
      .map(batch => writeHttp(parameters, batch))
      .toMat(Sink.head)(Keep.right)
  }

  private def writeHttp(precision: Option[WritePrecision], bucket: Option[String], org: Option[String], batch: Seq[AbstractWriteClient.BatchWriteData]): Done = {
    writeHttp(toWriteParameters(precision, bucket, org), batch)
  }

  private def writeHttp(parameters: WriteParameters, batch: Seq[AbstractWriteClient.BatchWriteData]): Done = {
    write(parameters, batch.toList.asJava.stream())
    Done.done()
  }

  private def toWriteParameters(precision: Option[WritePrecision], bucket: Option[String], org: Option[String]): WriteParameters = {
    val parameters = new WriteParameters(bucket.orNull, org.orNull, precision.orNull, null)
    parameters.check(options)
    parameters
  }
}
