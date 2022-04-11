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
package com.influxdb.client.kotlin.internal

import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.internal.AbstractWriteBlockingClient
import com.influxdb.client.internal.AbstractWriteClient
import com.influxdb.client.kotlin.WriteKotlinApi
import com.influxdb.client.service.WriteService
import com.influxdb.client.write.Point
import com.influxdb.client.write.WriteParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * @author Jakub Bednar (20/04/2021 9:27)
 */
internal class WriteKotlinApiImpl(service: WriteService, options: InfluxDBClientOptions) :
    AbstractWriteBlockingClient(service, options),
    WriteKotlinApi {

    override suspend fun writeRecord(record: String, precision: WritePrecision, bucket: String?, org: String?) {
        writeRecords(listOf(record), precision, bucket, org)
    }

    override suspend fun writeRecords(
        records: Iterable<String>,
        precision: WritePrecision,
        bucket: String?,
        org: String?
    ) {
        writeRecords(records.asFlow(), precision, bucket, org)
    }

    override suspend fun writeRecords(records: Flow<String>, precision: WritePrecision, bucket: String?, org: String?) {
        write(records.map { AbstractWriteClient.BatchWriteDataRecord(it) }, precision, bucket, org)
    }

    override suspend fun writePoint(point: Point, bucket: String?, org: String?) {
        writePoints(listOf(point), bucket, org)
    }

    override suspend fun writePoints(points: Iterable<Point>, bucket: String?, org: String?) {
        writePoints(points.asFlow(), bucket, org)
    }

    override suspend fun writePoints(points: Flow<Point>, bucket: String?, org: String?) {
        points
            .toList()
            .groupByTo(LinkedHashMap(), { it.precision }, { it })
            .forEach { group ->
                write(
                    group.value.asFlow().map { AbstractWriteClient.BatchWriteDataPoint(it, options) },
                    group.key,
                    bucket,
                    org
                )
            }
    }

    override suspend fun <M> writeMeasurement(
        measurement: M,
        precision: WritePrecision,
        bucket: String?,
        org: String?
    ) {
        writeMeasurements(listOf(measurement), precision, bucket, org)
    }

    override suspend fun <M> writeMeasurements(
        measurements: Iterable<M>,
        precision: WritePrecision,
        bucket: String?,
        org: String?
    ) {
        writeMeasurements(measurements.asFlow(), precision, bucket, org)
    }

    override suspend fun <M> writeMeasurements(
        measurements: Flow<M>,
        precision: WritePrecision,
        bucket: String?,
        org: String?
    ) {
        write(measurements.map { toMeasurementBatch(it, precision) }, precision, bucket, org)
    }

    private suspend fun write(
        records: Flow<AbstractWriteClient.BatchWriteData>,
        precision: WritePrecision,
        bucket: String?,
        org: String?
    ) {

        val bucketOrOption = bucket ?: options.bucket.orEmpty()
        val orgOrOption = org ?: options.org.orEmpty()

        write(records, WriteParameters(bucketOrOption, orgOrOption, precision))
    }

    private suspend fun write(
        records: Flow<AbstractWriteClient.BatchWriteData>,
        parameters: WriteParameters
    ) {

        write(parameters, records.toList().stream())
    }
}

