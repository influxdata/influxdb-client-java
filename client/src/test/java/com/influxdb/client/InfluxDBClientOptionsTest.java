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
package com.influxdb.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.InfluxException;
import com.influxdb.utils.TlsUtils;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:38)
 */
class InfluxDBClientOptionsTest {

    @Test
    void defaultValue() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .build();

        Assertions.assertThat(options.getUrl()).isEqualTo("http://localhost:9999/");
        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.TOKEN);
        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
        Assertions.assertThat(options.getPrecision()).isEqualTo(WritePrecision.NS);
    }

    @Test
    void okHttpBuilder() {

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        InfluxDBClientOptions options = InfluxDBClientOptions.builder().url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .okHttpClient(okHttpClient).build();

        Assertions.assertThat(options.getOkHttpClient()).isEqualTo(okHttpClient);
    }

    @Test
    void urlRequired() {

        InfluxDBClientOptions.Builder builder = InfluxDBClientOptions.builder()
                .authenticateToken("xyz".toCharArray());

        Assertions.assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to InfluxDB has to be defined.");
    }

    @Test
    void authorizationNone() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999")
                .build();

        Assertions.assertThat(options.getAuthScheme()).isNull();
    }

    @Test
    void authorizationSession() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999")
                .authenticate("user", "secret".toCharArray())
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.SESSION);
    }

    @Test
    void protocolVersion() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999")
                .authenticateToken("xyz".toCharArray())
                .build();

        List<Protocol> protocols = options.getOkHttpClient().build().protocols();
        Assertions.assertThat(protocols).hasSize(1);
        Assertions.assertThat(protocols).contains(Protocol.HTTP_1_1);
    }

    @Test
    void parseURLAsConnectionString() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999?readTimeout=1000&writeTimeout=3000&connectTimeout=2000&logLevel=HEADERS&token=my-token&bucket=my-bucket&org=my-org")
                .build();

        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.TOKEN);
        Assertions.assertThat(options.getToken()).isEqualTo("my-token".toCharArray());
        Assertions.assertThat(options.getBucket()).isEqualTo("my-bucket");
        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
    }

    @Test
    void keepBucketOrgSettingsIfAreBeforeURL() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .bucket("my-bucket")
                .org("my-org")
                .url("http://localhost:8086")
                .authenticateToken("my-token".toCharArray())
                .build();

        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
        Assertions.assertThat(options.getBucket()).isEqualTo("my-bucket");
    }

    @Test
    void okHttpBeforeURL() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .okHttpClient(new OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)))
                .url("http://localhost:8086")
                .build();

        Assertions.assertThat(options.getOkHttpClient().build().protocols()).containsExactly(Protocol.H2_PRIOR_KNOWLEDGE);
    }

    @Test
    public void customClientTypeFromConnectionString() {

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:9999?token=my-token&bucket=my-bucket&org=my-org&clientType=url-service")
                .build();

        Assertions.assertThat(options.getClientType()).isEqualTo("url-service");
    }

    @Test
    public void tlsFilesConfig() {
        String tlsDir = "src/test/java/com/influxdb/client/tls/";
        String influxdbCertPath = tlsDir + "influxdb.crt";
        String clientCertPath = tlsDir + "client.crt";
        String clientKeyPath = tlsDir + "client.key";
        String clientP12 = tlsDir + "client.p12";

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://localhost:8086")
                .trustFilePath(influxdbCertPath, null)
                .certificateFilePath(clientCertPath, clientKeyPath)
                .build();

        Assertions.assertThat(options.getTrustFilePath()).isEqualTo(influxdbCertPath);
        Assertions.assertThat(options.getCertificatePath()).isEqualTo(clientCertPath);
        Assertions.assertThat(options.getCertificateKeyPath()).isEqualTo(clientKeyPath);

        // For .p12 files
        var password = "changeit".toCharArray();
        InfluxDBClientOptions options1 = InfluxDBClientOptions.builder()
                .url("http://localhost:8086")
                .certificateP12FilePath(clientP12, password)
                .build();

        Assertions.assertThat(options1.getCertificateP12FilePath()).isEqualTo(clientP12);
        Assertions.assertThat(options1.getKeyPassword()).isEqualTo(password);
    }

    @Test
    public void customClientTypeFromProperties() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder().loadProperties().build();

        Assertions.assertThat(options.getClientType()).isEqualTo("properties-service");
    }

    @Test
    public void ipv6Loopback(){
        String[] loopbacks = {"[::1]", "[0000:0000:0000:0000:0000:0000:0000:0001]"};

      for (String loopback : loopbacks) {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
          .url(String.format("http://%s:9999/api/v2/", loopback))
          .authenticateToken("xyz".toCharArray())
          .org("my-org")
          .build();

        Assertions.assertThat(options.getUrl()).isEqualTo("http://[::1]:9999/api/v2/");
        Assertions.assertThat(options.getAuthScheme()).isEqualTo(InfluxDBClientOptions.AuthScheme.TOKEN);
        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
        Assertions.assertThat(options.getPrecision()).isEqualTo(WritePrecision.NS);
        Assertions.assertThat(options.getOrg()).isEqualTo("my-org");
      }
    }

    @Test
    public void ipv6General(){
        Map<String, String> ipv6Expected = Map.of(
          "[2001:db80:0001:1000:1100:0011:1110:0111]", "[2001:db80:1:1000:1100:11:1110:111]",
          "[2001:db8:1000:0000:0000:0000:0000:0001]", "[2001:db8:1000::1]",
          "[2001:db8f:0ff0:00ee:0ddd:000c:bbbb:aaaa]", "[2001:db8f:ff0:ee:ddd:c:bbbb:aaaa]",
          "[2001:0db8:0000:0000:0000:9876:0000:001f]", "[2001:db8::9876:0:1f]",
          "[0000:0000:0000:0000:0000:0000:0000:0000]", "[::]",
          "[2001:0db8:fedc:edcb:dcba:cba9:ba98:a987]", "[2001:db8:fedc:edcb:dcba:cba9:ba98:a987]"//,
          //"[::1]", ""
        );

        for(String key : ipv6Expected.keySet()){
            InfluxDBClientOptions options = InfluxDBClientOptions.builder()
              .url(String.format("http://%s:9999/api/v2/query?orgID=my-org", key))
              .authenticateToken("xyz".toCharArray())
              .build();

            System.out.println(key + ": " + options.getUrl());

            Assertions.assertThat(options.getUrl())
              .isEqualTo(String.format("http://%s:9999/api/v2/query/", ipv6Expected.get(key)));
            Assertions.assertThat(options.getToken())
              .isEqualTo("xyz".toCharArray());
        }
    }

    @Test
    public void ipv6Invalid(){
        List<String> invalidIpv6 = Arrays.asList(
          "[:1]",
          "[:::1]",
          "[2001:db8:0000:1]",
          "[2001:db8:00000::1]",
          "[2001:db8:0000:::1]",
          "[:0000::1]",
          "[:::0000::1]");
        for(String ipv6 : invalidIpv6){
            Assertions.assertThatThrownBy(() -> { InfluxDBClientOptions options2 = InfluxDBClientOptions.builder()
              .url(String.format("http://%s:9999/api/v2/query?orgID=my-org", ipv6))
              .authenticateToken("xyz".toCharArray())
              .build();}).isInstanceOf(InfluxException.class)
              .hasMessage(String.format("Unable to parse connection string http://%s:9999/api/v2/query?orgID=my-org", ipv6));
        }
    }

    @Test
    void tlsBothCertificateAndP12Configured() {
        Assertions.assertThatThrownBy(() -> InfluxDBClientOptions.builder()
                        .url("https://localhost:9999")
                        .certificateFilePath("cert.pem", "key.pem")
                        .certificateP12FilePath("client.p12", null)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot set both p12FilePath and certificatePath");
    }

    @Test
    void tlsOnlyClientCertificatesConfigured() {
        String clientCertPath = "src/test/java/com/influxdb/client/tls/client.crt";
        String clientKeyPath = "src/test/java/com/influxdb/client/tls/client.key";

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("https://localhost:9999")
                .certificateFilePath(clientCertPath, clientKeyPath)
                .build();

        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
    }

    @Test
    void tlsOnlyClientP12Configured() {
        String clientP12 = "src/test/java/com/influxdb/client/tls/client.p12";

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("https://localhost:9999")
                .certificateP12FilePath(clientP12, "changeit".toCharArray())
                .build();

        Assertions.assertThat(options.getOkHttpClient()).isNotNull();
    }

    @Test
    void tlsInvalidCertificatePath() {
        Assertions.assertThatThrownBy(() -> InfluxDBClientOptions.builder()
                        .url("https://localhost:9999")
                        .certificateFilePath("non_existing_file.pem", "non_existing_file.key")
                        .build())
                .isInstanceOf(InfluxException.class);
    }

    @Test
    void encryptedPemKeyIsRejected() {
        Assertions.assertThatThrownBy(() ->
                        TlsUtils.loadPrivateKey("src/test/java/com/influxdb/client/tls/client_pkcs8.key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Encrypted PKCS#8 private keys are not supported");
    }
}