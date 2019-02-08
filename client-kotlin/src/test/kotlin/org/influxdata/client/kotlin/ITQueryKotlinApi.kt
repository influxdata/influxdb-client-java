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
package org.influxdata.client.kotlin

import assertk.assert
import assertk.assertions.containsExactly
import assertk.assertions.endsWith
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.influxdata.annotations.Column
import org.influxdata.client.InfluxDBClientFactory
import org.influxdata.client.domain.Bucket
import org.influxdata.client.domain.Organization
import org.influxdata.client.domain.Permission
import org.influxdata.client.domain.PermissionResource
import org.influxdata.client.domain.ResourceType
import org.influxdata.client.domain.RetentionRule
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.net.ConnectException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * @author Jakub Bednar (bednar@github) (31/10/2018 07:16)
 */
@RunWith(JUnitPlatform::class)
internal class ITQueryKotlinApi : AbstractITInfluxDBClientKotlin() {

    private lateinit var bucket: Bucket
    private lateinit var organization: Organization
    private lateinit var queryKotlinApi: QueryKotlinApi

    @BeforeEach
    fun `Write testing data`(): Unit = runBlocking {

        val client = InfluxDBClientFactory.create(influxDb2Url, "my-user",
                "my-password".toCharArray())

        organization = client.organizationsApi
                .findOrganizations().stream()
                .filter { organization -> organization.name == "my-org" }
                .findFirst()
                .orElseThrow { IllegalStateException() }

        val retentionRule = RetentionRule()
        retentionRule.type = "expire"
        retentionRule.everySeconds = 3600L

        bucket = client.bucketsApi
                .createBucket(generateName("h2o"), retentionRule, organization)

        //
        // Add Permissions to read and write to the Bucket
        //

        val resource = PermissionResource()
        resource.orgID = organization.id
        resource.type = ResourceType.BUCKETS
        resource.id = bucket.id

        val readBucket = Permission()
        readBucket.resource = resource
        readBucket.action = Permission.READ_ACTION

        val writeBucket = Permission()
        writeBucket.resource = resource
        writeBucket.action = Permission.WRITE_ACTION

        val authorization = client.authorizationsApi
                .createAuthorization(organization, Arrays.asList(readBucket, writeBucket))

        val token = authorization.token

        val records = arrayOf("mem,host=A,region=west free=10i 10000000000",
                "mem,host=A,region=west free=11i 20000000000",
                "mem,host=B,region=west free=20i 10000000000",
                "mem,host=B,region=west free=22i 20000000000",
                "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
                "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
                "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
                .joinToString("\n")

        val writeApi = client.writeApi
        writeApi.writeRecord(bucket.name, organization.id, ChronoUnit.NANOS, records)
        writeApi.close()

        client.close()

        influxDBClient.close()
        influxDBClient = InfluxDBClientKotlinFactory.create(influxDb2Url, token.toCharArray())
        queryKotlinApi = influxDBClient.getQueryKotlinApi()
    }

    @Test
    fun `Simple query mapped to FluxRecords`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
                "|> sum()"

        val records = queryKotlinApi.query(flux, organization.id)

        val tables = records.toList()
        assert(tables).hasSize(2)
    }

    @Test
    fun `Simple query FluxRecords order`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t |> sort(columns:[\"value\"])"

        val records = queryKotlinApi.query(flux, organization.id)

        val values = records.map { it.value }.toList()

        assert(values).hasSize(10)
        assert(values).containsExactly(10L, 11L, 20L, 22L, 55L, 35L, 38L, 65L, 45L, 49L)
    }

    @Test
    fun `Mapping to POJO`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))"

        val query = queryKotlinApi.query(flux, organization.id, Mem::class.java)
        val memory = query.toList()

        assert(memory).hasSize(4)

        assert(memory[0].host).isEqualTo("A")
        assert(memory[0].region).isEqualTo("west")
        assert(memory[0].free).isEqualTo(10L)
        assert(memory[0].time).isEqualTo(Instant.ofEpochSecond(10))

        assert(memory[1].host).isEqualTo("A")
        assert(memory[1].region).isEqualTo("west")
        assert(memory[1].free).isEqualTo(11L)
        assert(memory[1].time).isEqualTo(Instant.ofEpochSecond(20))

        assert(memory[2].host).isEqualTo("B")
        assert(memory[2].region).isEqualTo("west")
        assert(memory[2].free).isEqualTo(20L)
        assert(memory[2].time).isEqualTo(Instant.ofEpochSecond(10))

        assert(memory[3].host).isEqualTo("B")
        assert(memory[3].region).isEqualTo("west")
        assert(memory[3].free).isEqualTo(22L)
        assert(memory[3].time).isEqualTo(Instant.ofEpochSecond(20))
    }

    @Test
    fun `Not running server`() {

        val clientNotRunning = InfluxDBClientKotlinFactory.create("http://localhost:8099")

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)"

        val channel = clientNotRunning.getQueryKotlinApi().query(flux, organization.id)

        assert { runBlocking { channel.toList() } }
                .thrownError { isInstanceOf(ConnectException::class.java) }

        assert(channel.isClosedForReceive).isTrue()
        assert(channel.isClosedForSend).isTrue()

        clientNotRunning.close()
    }

    @Test
    fun `Map to String`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\t" +
                "|> sum()"

        val lines = queryKotlinApi.queryRaw(flux, organization.id).toList()
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
    fun `Custom dialect`(): Unit = runBlocking {

        val flux = "from(bucket:\"${bucket.name}\")\n\t" +
                "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
                "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\t" +
                "|> sum()"

        val dialect = JSONObject()
                .put("header", false)
                .toString()

        val lines = queryKotlinApi.queryRaw(flux, dialect, organization.id).toList()

        assert(lines).hasSize(3)
        assert(lines[0]).endsWith(",free,mem,A,west,21")
        assert(lines[1]).endsWith(",free,mem,B,west,42")
        assert(lines[2]).isEmpty()
    }

    class Mem {

        internal val host: String? = null
        internal val region: String? = null

        @Column(name = "_value")
        internal val free: Long? = null
        @Column(name = "_time")
        internal val time: Instant? = null
    }
}