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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;

public class TlsExample {

    private static final String INFLUXDB_URL = "http://localhost:8086";
    private static final char[] AUTH_TOKEN = "my-token".toCharArray();
    private static final char[] DEFAULT_PASSWORD = "my-password".toCharArray();

    public static void main(final String[] args) {
        demonstrateServerTls();
        demonstrateMtlsCertAndKey();
        demonstrateMtlsP12();
    }

    /**
     * TLS configured by providing the server certificate (.p12, .crt, or .cert format).
     */
    private static void demonstrateServerTls() {
        var filePath = "path/to/cert.crt";
        InfluxDBClientOptions options = baseOptionsBuilder()
                .trustFilePath(filePath, DEFAULT_PASSWORD)
                .build();
        verifyConnection(options);
    }

    /**
     * mTLS configured by providing a client certificate (.cert, .crt) and private key (.key).
     */
    private static void demonstrateMtlsCertAndKey() {
        var certificatePath = "path/to/cert.crt";
        var certificateKeyPath = "path/to/cert.key";
        InfluxDBClientOptions options = baseOptionsBuilder()
                .certificateFilePath(certificatePath, certificateKeyPath)
                .build();
        verifyConnection(options);
    }

    /**
     * mTLS configured by providing a PKCS#12 client certificate bundle (.p12).
     */
    private static void demonstrateMtlsP12() {
        var certP12FilePath = "path/to/cert.p12";
        InfluxDBClientOptions options = baseOptionsBuilder()
                .certificateP12FilePath(certP12FilePath, DEFAULT_PASSWORD)
                .build();
        verifyConnection(options);
    }

    private static InfluxDBClientOptions.Builder baseOptionsBuilder() {
        return InfluxDBClientOptions.builder()
                .url(INFLUXDB_URL)
                .authenticateToken(AUTH_TOKEN);
    }

    private static void verifyConnection(final InfluxDBClientOptions options) {
        try (InfluxDBClient client = InfluxDBClientFactory.create(options)) {
            client.version();
        }
    }
}