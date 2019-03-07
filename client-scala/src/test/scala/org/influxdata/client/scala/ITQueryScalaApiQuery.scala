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
package org.influxdata.client.scala

import java.net.ConnectException
import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.influxdata.annotations.Column
import org.influxdata.client.InfluxDBClientFactory
import org.influxdata.client.domain._
import org.influxdata.exceptions.InfluxException
import org.influxdata.query.FluxRecord
import org.json.JSONObject
import org.scalatest.Matchers

import scala.collection.JavaConverters._

/**
 * @author Jakub Bednar (bednar@github) (07/11/2018 10:00)
 */
class ITQueryScalaApiQuery extends AbstractITQueryScalaApi with Matchers {

  implicit val system: ActorSystem = ActorSystem("it-tests")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  var organization: Organization = _
  var queryScalaApi: QueryScalaApi = _
  var fluxPrefix: String = _

  before {

    super.setUp()

    val client = InfluxDBClientFactory.create(influxDBUtils.getUrl, "my-user", "my-password".toCharArray)

    organization = client.getOrganizationsApi
      .findOrganizations()
      .stream()
      .filter(o => o.getName == "my-org").findFirst()
      .orElseThrow(() => new IllegalStateException())

    val retentionRule = new RetentionRule()
    retentionRule.setType("expire")
    retentionRule.setEverySeconds(3600L)

    val bucket = client.getBucketsApi.createBucket(influxDBUtils.generateName("h2o"), retentionRule, organization)
    fluxPrefix = "from(bucket:\"" + bucket.getName + "\")\n\t"

    //
    // Add Permissions to read and write to the Bucket
    //

    val resource = new PermissionResource
    resource.setOrgID(organization.getId)
    resource.setType(ResourceType.BUCKETS)
    resource.setId(bucket.getId)

    val readBucket = new Permission
    readBucket.setResource(resource)
    readBucket.setAction(Permission.READ_ACTION)

    val writeBucket = new Permission
    writeBucket.setResource(resource)
    writeBucket.setAction(Permission.WRITE_ACTION)

    val authorization = client.getAuthorizationsApi
      .createAuthorization(organization, List(readBucket, writeBucket).asJava)

    val token = authorization.getToken

    val records = Seq("mem,host=A,region=west free=10i 10000000000",
      "mem,host=A,region=west free=11i 20000000000",
      "mem,host=B,region=west free=20i 10000000000",
      "mem,host=B,region=west free=22i 20000000000",
      "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
      "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
      "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
      .mkString("\n")

    val writeApi = client.getWriteApi
    writeApi.writeRecord(bucket.getName, organization.getId, ChronoUnit.NANOS, records)
    writeApi.close()

    client.close()

    influxDBClient.close()
    influxDBClient = InfluxDBClientScalaFactory.create(influxDBUtils.getUrl, token.toCharArray)
    queryScalaApi = influxDBClient.getQueryScalaApi()
  }

  test("Simple query mapped to FluxRecords") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
      "|> sum()"

    val source = queryScalaApi.query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val record1 = source.requestNext()

    record1.getMeasurement should be("mem")
    record1.getValue should be(21)

    val record2 = source.requestNext()

    record2.getMeasurement should be("mem")
    record2.getValue should be(42)

    source.expectComplete()
  }

  test("Simple query FluxRecords order") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> sort(columns:[\"value\"])"

    val source = queryScalaApi.query(flux, organization.getId).map(it => it.getValue).runWith(TestSink.probe[Object])

    source.requestNext() should be(10L)
    source.requestNext() should be(11L)
    source.requestNext() should be(20L)
    source.requestNext() should be(22L)
    source.requestNext() should be(35L)
    source.requestNext() should be(38L)
    source.requestNext() should be(55L)
    source.requestNext() should be(45L)
    source.requestNext() should be(49L)
    source.requestNext() should be(65L)

    source.expectComplete()
  }

  test("Mapping to POJO") {

    val flux = fluxPrefix + "" +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))"

    val source = queryScalaApi.query(flux, organization.getId, classOf[Mem]).runWith(TestSink.probe[Mem])

    var mem = source.requestNext()
    mem.host should be("A")
    mem.region should be("west")
    mem.free should be(10L)
    mem.time should be(Instant.ofEpochSecond(10))

    mem = source.requestNext()
    mem.host should be("A")
    mem.region should be("west")
    mem.free should be(11L)
    mem.time should be(Instant.ofEpochSecond(20))

    mem = source.requestNext()
    mem.host should be("B")
    mem.region should be("west")
    mem.free should be(20L)
    mem.time should be(Instant.ofEpochSecond(10))

    mem = source.requestNext()
    mem.host should be("B")
    mem.region should be("west")
    mem.free should be(22L)
    mem.time should be(Instant.ofEpochSecond(20))

    source.expectComplete()
  }

  test("Error propagation") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
      "|> sum_wrong()"

    val source = queryScalaApi.query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[InfluxException]
  }

  test("Not running server") {

    val clientNotRunning = InfluxDBClientScalaFactory.create("http://localhost:8099")

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)"

    val source = clientNotRunning.getQueryScalaApi().query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[ConnectException]

    clientNotRunning.close()
  }

  test("Map to String") {

    val flux = fluxPrefix  +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
      "|> sum()"

    val source = queryScalaApi.queryRaw(flux, organization.getId).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should be("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,string,string,string,string,long")

    line = source.requestNext()
    line should be("#group,false,false,true,true,true,true,true,true,false")

    line = source.requestNext()
    line should be("#default,_result,,,,,,,,")

    line = source.requestNext()
    line should be(",result,table,_start,_stop,_field,_measurement,host,region,_value")

    line = source.requestNext()
    line should endWith(",free,mem,A,west,21")

    line = source.requestNext()
    line should endWith(",free,mem,B,west,42")

    line = source.requestNext()
    line shouldBe empty

    source.expectComplete()
  }

  test("Custom dialect") {

    val flux = fluxPrefix  +
      "|> range(start: 1970-01-01T00:00:00.000000000Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
      "|> sum()"

    val dialect = new JSONObject()
      .put("header", false)
      .toString()

    val source = queryScalaApi.queryRaw(flux, dialect, organization.getId).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should endWith(",free,mem,A,west,21")

    line = source.requestNext()
    line should endWith(",free,mem,B,west,42")

    line = source.requestNext()
    line shouldBe empty

    source.expectComplete()
  }

}

class Mem {

  var host: String = _
  var region: String = _

  @Column(name = "_value")
  var free: java.lang.Long = _
  @Column(name = "_time")
  var time: Instant = _
}
