/*
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
package example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.BackpressureEvent;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.client.write.events.WriteSuccessEvent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.CountingInputStream;

public class CsvImportExample {
    public static Logger log = Logger.getLogger(CsvImportExample.class.getName());

    public static void main(String[] args) {
        new CsvImportExample();
    }

    String url = "http://localhost:9999";
    final String token = "my-token";
    final String bucket = "my-bucket";
    final String org = "my-org";
    final int batchSize = 10_000;
    final int flushInterval = 1000;
    final int bufferLimit = 1_000_000;
    WriteApi writeApi;

    public CsvImportExample() {
        String[] files = {
//            "file:///Volumes/ADATA/test/fhv_tripdata_2019-01.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-01.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-02.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-03.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-04.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-05.csv",
            "https://s3.amazonaws.com/nyc-tlc/trip+data/fhv_tripdata_2019-06.csv"
        };

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
            .url(url)
            .authenticateToken(token.toCharArray())
            .bucket(bucket)
            .org(org)
            .build();

        InfluxDBClient client = InfluxDBClientFactory.create(options);
        client.setLogLevel(LogLevel.NONE);

        WriteOptions writeOptions = WriteOptions.builder()
            .batchSize(batchSize)
            .flushInterval(flushInterval)
            .bufferLimit(bufferLimit)
            .build();

        writeApi = client.getWriteApi(writeOptions);

        writeApi.listenEvents(BackpressureEvent.class, e -> System.out.print("BackpressureEvent - "));
        writeApi.listenEvents(WriteErrorEvent.class, e -> System.out.print("WriteErrorEvent - " + e.getThrowable().toString()));
        writeApi.listenEvents(WriteSuccessEvent.class,
            e -> System.out.println("WriteSuccessEvent - " + e.getLineProtocol().split("\n").length + " lines."));

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        //submit jobs
        Stream.of(files).forEach(file -> executorService.submit(new WriteJob(file)));
        //wait for all tasks are finished
        executorService.shutdown();
    }

    private class WriteJob implements Runnable {

        private String file;
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        private Point parse(final CSVRecord record) {
            try {
                Date pickup_datetime = sdf.parse(record.get("pickup_datetime"));
                return Point.measurement("taxi-trip-data")
                    .addTag("dispatching_base_num", record.get("dispatching_base_num"))
                    .addTag("PULocationID", record.get("PULocationID"))
                    .addTag("DOLocationID", record.get("DOLocationID"))
                    .addTag("SR_Flag", record.get("SR_Flag"))
                    .addField("dropoff_datetime", record.get("dropoff_datetime"))
                    .time(pickup_datetime.toInstant(), WritePrecision.NS);

            } catch (Exception e) {
                log.log(Level.SEVERE, "Unable to parse point timestamp", e);
            }
            return null;
        }

        public WriteJob(final String file) {
            this.file = file;
        }

        @Override
        public void run() {
            System.out.printf("Processing file %s, Thread: %s \n", file, Thread.currentThread().getId());
            CSVFormat csvConfig = CSVFormat.DEFAULT.withHeader();
            long counter = 0;

            long fileLength = 0;
            URLConnection urlConnection = null;
            try {
                urlConnection = new URL(file).openConnection();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
                return;
            }

            try (
                CountingInputStream countingInputStream = new CountingInputStream(urlConnection.getInputStream());
                CSVParser parser = new CSVParser(new InputStreamReader(countingInputStream), csvConfig)
            ) {
                fileLength = urlConnection.getContentLength();

                for (final CSVRecord next : parser) {
                    if (counter++ % 10000 == 0) {
                        // print progress %
                        System.out.printf("%d - %s - %.2f%% \n", counter, file, 100 * (float) countingInputStream.getCount() / (float) fileLength);
                    }
                    Point p = parse(next);
                    if (p != null) {
                        writeApi.writePoint(p);
                    }
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "IO error", e);
            }
        }
    }
}
