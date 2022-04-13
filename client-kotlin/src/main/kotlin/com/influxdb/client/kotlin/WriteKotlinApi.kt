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

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.WriteParameters
import com.influxdb.client.write.Point
import kotlinx.coroutines.flow.Flow

/**
 * The Kotlin API to write time-series data into InfluxDB 2.x.
 *
 * @author Jakub Bednar (20/04/2021 8:18)
 */
interface WriteKotlinApi {

    /**
     * Write Line Protocol record into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param record        specified in [LineProtocol](http://bit.ly/line-protocol). The `record` is considered as one batch unit.
     * @param precision     precision for the unix timestamps within the body line-protocol
     * @param bucket        specifies the destination bucket for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                      `bucket` if the `bucket` is not specified.
     * @param org           specifies the destination organization for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                      `organization` if the `org` is not specified.
     */
    suspend fun writeRecord(record: String, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Line Protocol records into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param records   specified in [LineProtocol](http://bit.ly/line-protocol).
     *                  The `records` are considered as one batch unit.
     * @param precision precision for the unix timestamps within the body line-protocol
     * @param bucket    specifies the destination bucket for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                  `bucket` if the `bucket` is not specified.
     * @param org       specifies the destination organization for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                  `organization` if the `org` is not specified.
     */
    suspend fun writeRecords(records: Iterable<String>, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Line Protocol records into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param records   specified in [LineProtocol](http://bit.ly/line-protocol).
     *                  The `records` are considered as one batch unit.
     * @param precision precision for the unix timestamps within the body line-protocol
     * @param bucket    specifies the destination bucket for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                  `bucket` if the `bucket` is not specified.
     * @param org       specifies the destination organization for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                  `organization` if the `org` is not specified.
     */
    suspend fun writeRecords(records: Flow<String>, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Line Protocol records into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param records       specified in [LineProtocol](http://bit.ly/line-protocol).
     *                      The `records` are considered as one batch unit.
     * @param parameters    specify InfluxDB Write endpoint parameters
     */
    suspend fun writeRecords(records: Flow<String>, parameters: WriteParameters)

    /**
     * Write Data Point into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param point     specified data point. The `point` is considered as one batch unit.
     * @param bucket    specifies the destination bucket for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                  `bucket` if the `bucket` is not specified.
     * @param org       specifies the destination organization for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                  `organization` if the `org` is not specified.
     */
    suspend fun writePoint(point: Point, bucket: String? = null, org: String? = null)

    /**
     * Write Data Points into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param points    specified data points. The `points` are considered as one batch unit.
     * @param bucket    specifies the destination bucket for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                  `bucket` if the `bucket` is not specified.
     * @param org       specifies the destination organization for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                  `organization` if the `org` is not specified.
     */
    suspend fun writePoints(points: Iterable<Point>, bucket: String? = null, org: String? = null)

    /**
     * Write Data Points into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param points    specified data points. The `points` are considered as one batch unit.
     * @param bucket    specifies the destination bucket for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                  `bucket` if the `bucket` is not specified.
     * @param org       specifies the destination organization for writes.
     *                  The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                  `organization` if the `org` is not specified.
     */
    suspend fun writePoints(points: Flow<Point>, bucket: String? = null, org: String? = null)

    /**
     * Write Data Points into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param points        specified data points. The `points` are considered as one batch unit.
     * @param parameters    specify InfluxDB Write endpoint parameters
     */
    suspend fun writePoints(points: Flow<Point>, parameters: WriteParameters)

    /**
     * Write Measurement into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param measurement   specified Measurement. The `measurement` is considered as one batch unit.
     * @param precision     precision for the unix timestamps within the body line-protocol
     * @param bucket        specifies the destination bucket for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                      `bucket` if the `bucket` is not specified.
     * @param org           specifies the destination organization for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                      `organization` if the `org` is not specified.
     * @param <M>           measurement type
     */
    suspend fun <M> writeMeasurement(measurement: M, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Measurements into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param measurements  specified Measurements. The `measurements` are considered as one batch unit.
     * @param precision     precision for the unix timestamps within the body line-protocol
     * @param bucket        specifies the destination bucket for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                      `bucket` if the `bucket` is not specified.
     * @param org           specifies the destination organization for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                      `organization` if the `org` is not specified.
     * @param <M>           measurement type
     */
    suspend fun <M> writeMeasurements(measurements: Iterable<M>, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Measurements into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param measurements  specified Measurements. The `measurements` are considered as one batch unit.
     * @param precision     precision for the unix timestamps within the body line-protocol
     * @param bucket        specifies the destination bucket for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getBucket] will be used as the destination
     *                      `bucket` if the `bucket` is not specified.
     * @param org           specifies the destination organization for writes.
     *                      The [com.influxdb.client.InfluxDBClientOptions.getOrg] will be used as the destination
     *                      `organization` if the `org` is not specified.
     * @param <M>           measurement type
     */
    suspend fun <M> writeMeasurements(measurements: Flow<M>, precision: WritePrecision, bucket: String? = null, org: String? = null)

    /**
     * Write Measurements into InfluxDB.
     *
     * If any exception occurs during write, this exception is rethrown from this method.
     *
     * @param measurements  specified Measurements. The `measurements` are considered as one batch unit.
     * @param parameters    specify InfluxDB Write endpoint parameters
     */
    suspend fun <M> writeMeasurements(measurements: Flow<M>, parameters: WriteParameters)
}