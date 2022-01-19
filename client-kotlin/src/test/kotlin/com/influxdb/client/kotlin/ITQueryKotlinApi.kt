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

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.Bucket
import com.influxdb.client.domain.Dialect
import com.influxdb.client.domain.Organization
import com.influxdb.client.domain.Permission
import com.influxdb.client.domain.PermissionResource
import com.influxdb.client.domain.Query
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.internal.AbstractInfluxDBClient
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.net.ConnectException
import java.time.Instant
import java.util.*

/**
 * @author Jakub Bednar (bednar@github) (31/10/2018 07:16)
 */
@RunWith(JUnitPlatform::class)
internal class ITQueryKotlinApi : AbstractITInfluxDBClientKotlin() {

    private lateinit var bucket: Bucket
    private lateinit var token: String
    private lateinit var organization: Organization
    private lateinit var queryKotlinApi: QueryKotlinApi

    @BeforeEach
    fun `Write testing data`(): Unit = runBlocking {

        val client = InfluxDBClientFactory.create(influxDb2Url, "my-token".toCharArray())

        organization = client.organizationsApi
                .findOrganizations().stream()
                .filter { organization -> organization.name == "my-org" }
                .findFirst()
                .orElseThrow { IllegalStateException() }

        bucket = client.bucketsApi
                .createBucket(generateName("h2o"), null, organization)

        //
        // Add Permissions to read and write to the Bucket
        //

        val resource = PermissionResource()
        resource.orgID = organization.id
        resource.type = PermissionResource.TYPE_BUCKETS
        resource.id = bucket.id

        val readBucket = Permission()
        readBucket.resource = resource
        readBucket.action = Permission.ActionEnum.READ

        val writeBucket = Permission()
        writeBucket.resource = resource
        writeBucket.action = Permission.ActionEnum.WRITE

        val authorization = client.authorizationsApi
                .createAuthorization(organization, Arrays.asList(readBucket, writeBucket))

        token = authorization.token

        val records = arrayOf("mem,host=A,region=west free=10i 10000000000",
                "mem,host=A,region=west free=11i 20000000000",
                "mem,host=B,region=west free=20i 10000000000",
                "mem,host=B,region=west free=22i 20000000000",
                "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
                "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
                "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
                .joinToString("\n")

        val writeApi = client.writeApi
        writeApi.writeRecord(bucket.name, organization.id, WritePrecision.NS, records)

        client.close()

        influxDBClient.close()
        influxDBClient = InfluxDBClientKotlinFactory.create(influxDb2Url, token.toCharArray())
        queryKotlinApi = influxDBClient.getQueryKotlinApi()
    }

    @Test
    fun `Simple query mapped to FluxRecords`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
                "|> sum()"

        val records = queryKotlinApi.query(flux, organization.id)

        val tables = records.toList()
        assertThat(tables).hasSize(2)
    }

    @Test
    fun `Default Org Bucket`(): Unit = runBlocking {

        influxDBClient.close()
        influxDBClient = InfluxDBClientKotlinFactory.create(influxDb2Url, token.toCharArray(), "my-org", "my-bucket")
        queryKotlinApi = influxDBClient.getQueryKotlinApi()

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
                "|> sum()"

        // String
        run {
            val records = queryKotlinApi.query(flux)

            val tables = records.toList()
            assertThat(tables).hasSize(2)
        }

        // Query
        run {
            val records = queryKotlinApi.query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(flux))

            val tables = records.toList()
            assertThat(tables).hasSize(2)
        }

        // String Measurement
        run {
            val records = queryKotlinApi.query(flux, Mem::class.java)

            val memory = records.toList()
            assertThat(memory).hasSize(2)
        }

        // Query Measurement
        run {
            val records = queryKotlinApi.query(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(flux), Mem::class.java)

            val memory = records.toList()
            assertThat(memory).hasSize(2)
        }

        // String Raw
        run {
            val lines = queryKotlinApi.queryRaw(flux).toList()

            assertThat(lines).hasSize(7)
            assertThat(lines[0]).startsWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")
            assertThat(lines[1]).startsWith("#group,false,false,true,true,")
            assertThat(lines[2]).isEqualTo("#default,_result,,,,,,,,")
            assertThat(lines[3]).startsWith(",result,table,_start,_stop,")
            assertThat(lines[4].contains(",42") ||
                    lines[4].contains(",21"), name = lines[4]).isTrue()
            assertThat(lines[5].contains(",42") ||
                    lines[5].contains(",21"), name = lines[5]).isTrue()
            assertThat(lines[6]).isEmpty()
        }

        // String Raw Dialect
        run {
            val lines = queryKotlinApi.queryRaw(flux, AbstractInfluxDBClient.DEFAULT_DIALECT).toList()

            assertThat(lines).hasSize(7)
            assertThat(lines[0]).startsWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")
            assertThat(lines[1]).startsWith("#group,false,false,true,true,")
            assertThat(lines[2]).isEqualTo("#default,_result,,,,,,,,")
            assertThat(lines[3]).startsWith(",result,table,_start,_stop,")
            assertThat(lines[4].contains(",42") ||
                    lines[4].contains(",21"), name = lines[4]).isTrue()
            assertThat(lines[5].contains(",42") ||
                    lines[5].contains(",21"), name = lines[5]).isTrue()
            assertThat(lines[6]).isEmpty()
        }

        // Query Raw
        run {
            val lines = queryKotlinApi.queryRaw(Query().dialect(AbstractInfluxDBClient.DEFAULT_DIALECT).query(flux)).toList()

            assertThat(lines).hasSize(7)
            assertThat(lines[0]).startsWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")
            assertThat(lines[1]).startsWith("#group,false,false,true,true,")
            assertThat(lines[2]).isEqualTo("#default,_result,,,,,,,,")
            assertThat(lines[3]).startsWith(",result,table,_start,_stop,")
            assertThat(lines[4].contains(",42") ||
                    lines[4].contains(",21"), name = lines[4]).isTrue()
            assertThat(lines[5].contains(",42") ||
                    lines[5].contains(",21"), name = lines[5]).isTrue()
            assertThat(lines[6]).isEmpty()
        }
    }

    @Test
    fun `Simple query FluxRecords order`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t "+
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
                "|> sort(columns:[\"value\"])"

        val records = queryKotlinApi.query(flux, organization.id)

        val values = records.toList().map { it.value }

        assertThat(values).hasSize(2)
        assertThat(values).containsExactly(10L, 11L)
    }

    @Test
    fun `Mapping to POJO`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))"

        val query = queryKotlinApi.query(flux, organization.id, Mem::class.java)
        val memory = query.toList()

        assertThat(memory).hasSize(2)

        assertThat(memory[0].host).isEqualTo("A")
        assertThat(memory[0].region).isEqualTo("west")
        assertThat(memory[0].free).isEqualTo(10L)
        assertThat(memory[0].time).isEqualTo(Instant.ofEpochSecond(10))

        assertThat(memory[1].host).isEqualTo("A")
        assertThat(memory[1].region).isEqualTo("west")
        assertThat(memory[1].free).isEqualTo(11L)
        assertThat(memory[1].time).isEqualTo(Instant.ofEpochSecond(20))
    }

    @Test
    fun `Not running server`() {

        val clientNotRunning = InfluxDBClientKotlinFactory.create("http://localhost:8099")

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)"

        val channel = clientNotRunning.getQueryKotlinApi().query(flux, organization.id)

        assertThat {
            runBlocking { channel.toList() }
        }.isFailure().isInstanceOf(ConnectException::class.java)

        assertThat(channel.isClosedForReceive).isTrue()
        assertThat(channel.isClosedForSend).isTrue()

        clientNotRunning.close()
    }

    @Test
    fun `Map to String`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
                "|> sum()"

        val lines = queryKotlinApi.queryRaw(flux, organization.id).toList()
        assertThat(lines).hasSize(6)
        assertThat(lines[0]).startsWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")
        assertThat(lines[1]).startsWith("#group,false,false,true,true,")
        assertThat(lines[2]).isEqualTo("#default,_result,,,,,,,,")
        assertThat(lines[3]).startsWith(",result,table,_start,_stop,")
        assertThat(lines[4]).contains(",21")
        assertThat(lines[4]).contains(",A")
        assertThat(lines[4]).contains(",west")
        assertThat(lines[5]).isEmpty()
    }

    @Test
    fun `Custom dialect`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))"  +
                "|> sum()"

        val dialect = Dialect().header(false)

        val lines = queryKotlinApi.queryRaw(flux, dialect, organization.id).toList()

        assertThat(lines).hasSize(2)
        assertThat(lines[0]).contains(",21")
        assertThat(lines[0]).contains(",A")
        assertThat(lines[0]).contains(",west")
        assertThat(lines[1]).isEmpty()
    }

    @Measurement(name = "mem")
    class Mem {

        @Column(tag = true)
        internal var host: String? = null
        @Column(tag = true)
        internal var region: String? = null

        @Column(name = "_value")
        internal var free: Long? = null
        @Column(name = "_time", timestamp = true)
        internal var time: Instant? = null
    }
}