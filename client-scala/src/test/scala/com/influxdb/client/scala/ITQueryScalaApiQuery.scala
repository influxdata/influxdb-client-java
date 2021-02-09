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

import akka.actor.ActorSystem
import akka.stream.scaladsl.FileIO
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import com.influxdb.annotations.Column
import com.influxdb.client.domain._
import com.influxdb.client.internal.AbstractInfluxDBClient
import com.influxdb.client.{InfluxDBClientFactory, InfluxDBClientOptions}
import com.influxdb.exceptions.InfluxException
import com.influxdb.query.FluxRecord
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

/**
 * @author Jakub Bednar (bednar@github) (07/11/2018 10:00)
 */
class ITQueryScalaApiQuery extends AbstractITQueryScalaApi with Matchers {

  implicit val system: ActorSystem = ActorSystem("it-tests")

  var organization: Organization = _
  var queryScalaApi: QueryScalaApi = _
  var fluxPrefix: String = _
  var token: String = _
  var bucket: Bucket = _

  before {

    super.setUp()

    val client = InfluxDBClientFactory.create(influxDBUtils.getUrl, "my-user", "my-password".toCharArray)

    organization = client.getOrganizationsApi
      .findOrganizations()
      .stream()
      .filter(o => o.getName == "my-org").findFirst()
      .orElseThrow(() => new IllegalStateException())

    bucket = client.getBucketsApi.createBucket(influxDBUtils.generateName("h2o"), null, organization)
    fluxPrefix = "from(bucket:\"" + bucket.getName + "\")\n\t"

    //
    // Add Permissions to read and write to the Bucket
    //

    val resource = new PermissionResource
    resource.setOrgID(organization.getId)
    resource.setType(PermissionResource.TypeEnum.BUCKETS)
    resource.setId(bucket.getId)

    val readBucket = new Permission
    readBucket.setResource(resource)
    readBucket.setAction(Permission.ActionEnum.READ)

    val writeBucket = new Permission
    writeBucket.setResource(resource)
    writeBucket.setAction(Permission.ActionEnum.WRITE)

    val authorization = client.getAuthorizationsApi
      .createAuthorization(organization, List(readBucket, writeBucket).asJava)

    token = authorization.getToken

    val records = Seq("mem,host=A,region=west free=10i 10000000000",
      "mem,host=A,region=west free=11i 20000000000",
      "mem,host=B,region=west free=20i 10000000000",
      "mem,host=B,region=west free=22i 20000000000",
      "cpu,host=A,region=west usage_system=35i,user_usage=45i 10000000000",
      "cpu,host=A,region=west usage_system=38i,user_usage=49i 20000000000",
      "cpu,host=A,hyper-threading=true,region=west usage_system=55i,user_usage=65i 20000000000")
      .mkString("\n")

    val writeApi = client.getWriteApi
    writeApi.writeRecord(bucket.getName, organization.getId, WritePrecision.NS, records)

    client.close()

    influxDBClient.close()
    influxDBClient = InfluxDBClientScalaFactory.create(influxDBUtils.getUrl, token.toCharArray)
    queryScalaApi = influxDBClient.getQueryScalaApi()
  }

  test("Simple query mapped to FluxRecords") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
      "|> sum()"

    val source = queryScalaApi.query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val record1 = source.requestNext()

    record1.getMeasurement should be("mem")
    record1.getValue should be(21)

    // end is signaled by None
    source.request(1)
    source.expectComplete()
  }

  test("Default Org Bucket") {

    influxDBClient.close()
    influxDBClient = InfluxDBClientScalaFactory.create(influxDBUtils.getUrl, token.toCharArray, "my-org")
    queryScalaApi = influxDBClient.getQueryScalaApi()

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
      "|> sum()"

    // String
    {
      val source = queryScalaApi.query(flux).runWith(TestSink.probe[FluxRecord])

      val record1 = source.requestNext()

      record1.getMeasurement should be("mem")
      record1.getValue should be(21)

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // Query
    {
      val source = queryScalaApi.query(new Query().query(flux).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT)).runWith(TestSink.probe[FluxRecord])

      val record1 = source.requestNext()

      record1.getMeasurement should be("mem")
      record1.getValue should be(21)

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // String Measurement
    {
      val source = queryScalaApi.query(flux, classOf[Mem]).runWith(TestSink.probe[Mem])

      val mem = source.requestNext()
      mem.free should be(21L)

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // Query Measurement
    {
      val source = queryScalaApi.query(new Query().query(flux).dialect(AbstractInfluxDBClient.DEFAULT_DIALECT), classOf[Mem]).runWith(TestSink.probe[Mem])

      val mem = source.requestNext()
      mem.free should be(21L)

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // String raw
    {
      val source = queryScalaApi.queryRaw(flux).runWith(TestSink.probe[String])

      var line = source.requestNext()
      line should startWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")

      line = source.requestNext()
      line should startWith("#group,false,false,true,true,")

      line = source.requestNext()
      line should be("#default,_result,,,,,,,,")

      line = source.requestNext()
      line should startWith(",result,table,_start,_stop,")

      line = source.requestNext()
      line should include(",21")

      line = source.requestNext()
      line shouldBe empty

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // String raw - dialect
    {
      val dialect = new Dialect()
        .header( false)

      val source = queryScalaApi.queryRaw(flux, dialect).runWith(TestSink.probe[String])

      var line = source.requestNext()

      line should include(",21")

      line = source.requestNext()
      line shouldBe empty

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }

    // Query raw
    {
      val dialect = new Dialect()
        .header( false)

      val source = queryScalaApi.queryRaw(new Query().query(flux).dialect(dialect)).runWith(TestSink.probe[String])

      var line = source.requestNext()

      line should include(",21")

      line = source.requestNext()
      line shouldBe empty

      // end is signaled by None
      source.request(1)
      source.expectComplete()
    }
  }

  test("Simple query FluxRecords order") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))"+
      "|> sort(columns:[\"value\"])"

    val source = queryScalaApi.query(flux, organization.getId).map(it => it.getValue).runWith(TestSink.probe[Object])

    source.requestNext() should be(10L)
    source.requestNext() should be(11L)
    source.expectComplete()
  }

  test("Mapping to POJO") {

    val flux = fluxPrefix + "" +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))"

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

    source.expectComplete()
  }

  test("Error propagation") {

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\"))\n\t" +
      "|> sum_wrong()"

    val source = queryScalaApi.query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[InfluxException]
  }

  test("Not running server") {

    val clientNotRunning = InfluxDBClientScalaFactory.create("http://localhost:8099")

    val flux = fluxPrefix +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)"

    val source = clientNotRunning.getQueryScalaApi().query(flux, organization.getId).runWith(TestSink.probe[FluxRecord])

    val throwable = source.expectSubscriptionAndError()

    throwable shouldBe a[InfluxException]
    throwable.getMessage contains "Failed to connect"

    clientNotRunning.close()
  }

  test("Map to String") {

    val flux = fluxPrefix  +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))" +
      "|> sum()"

    val source = queryScalaApi.queryRaw(flux, organization.getId).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should startWith("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,")

    line = source.requestNext()
    line should startWith("#group,false,false,true,true,")

    line = source.requestNext()
    line should be("#default,_result,,,,,,,,")

    line = source.requestNext()
    line should startWith(",result,table,_start,_stop,")

    line = source.requestNext()
    line should (include(",21") and include(",A") and include(",west"))

    line = source.requestNext()
    line shouldBe empty

    // end is signaled by None
    source.request(1)
    source.expectComplete()
  }

  test("Custom dialect") {

    val flux = fluxPrefix  +
      "|> range(start: 1970-01-01T00:00:00.000000001Z)\n\t" +
      "|> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" and r[\"_field\"] == \"free\" and r[\"host\"] == \"A\"))\n\t" +
      "|> sum()"

    val dialect = new Dialect()
      .header( false)

    val source = queryScalaApi.queryRaw(flux, dialect, organization.getId).runWith(TestSink.probe[String])

    var line = source.requestNext()
    line should (include(",21") and include(",A") and include(",west"))

    line = source.requestNext()
    line shouldBe empty

    // end is signaled by None
    source.request(1)
    source.expectComplete()
  }

  test("Exhausted too early") {

    influxDBClient.close()

    //
    // Prepare data
    //
    val records = Range(0, 10000).map(n => s"buffer field=$n $n").mkString("\n")
    val client = InfluxDBClientFactory.create(influxDBUtils.getUrl, "my-user", "my-password".toCharArray)
    client.getWriteApi.writeRecord(bucket.getName, organization.getId, WritePrecision.NS, records)
    client.close()

    //
    // Init client with small buffer
    //
    val influxOptions = InfluxDBClientOptions.builder()
      .url(influxDBUtils.getUrl)
      .authenticateToken(token.toCharArray)
      .org(organization.getName)
      .build()

    influxDBClient = InfluxDBClientScalaFactory
          .create(influxOptions)

    //
    // Query ALL
    //
    queryScalaApi = influxDBClient.getQueryScalaApi()

    val flux = s"$fluxPrefix |> range(start: 1970-01-01T00:00:00.000000001Z) " +
      "|> filter(fn: (r) => r[\"_measurement\"] == \"buffer\")"

    var count = 0
    val source = queryScalaApi.queryRaw(flux)
      .map(s => {
        count += 1
        ByteString(s + "\n")
      }).runWith(FileIO.toPath(Paths.get("./test.txt")))

    Await.result(source, Duration.Inf)
    count shouldBe 10004
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
