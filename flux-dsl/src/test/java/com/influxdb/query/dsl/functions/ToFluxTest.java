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
package com.influxdb.query.dsl.functions;

import java.util.Arrays;
import java.util.List;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/10/2018 09:35)
 */
@RunWith(JUnitPlatform.class)
class ToFluxTest {

    @Test
    void to() {

        Flux flux = Flux
                .from("telegraf")
                .to()
                    .withBucketID("O1")
                    .withOrgID("02")
                    .withHost("example.com")
                    .withToken("secret")
                    .withTimeColumn("timestamp")
                    .withTagColumns(new String[]{"location", "production"})
                    .withFieldFunction("return {\"hum\": r.hum, \"temp\": r.temp}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucketID: \"O1\", orgID: \"02\", host: \"example.com\", token: \"secret\", timeColumn: \"timestamp\", tagColumns: [\"location\", \"production\"], fieldFn: (r) => return {\"hum\": r.hum, \"temp\": r.temp})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void toBucketOrg() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org");

        Assertions.assertThat(flux.toString())
                .isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> to(bucket: \"my-bucket\", org: \"my-org\")");
    }

    @Test
    void toBucketOrgFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "return {\"hum\": r.hum}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucket: \"my-bucket\", org: \"my-org\", fieldFn: (r) => return {\"hum\": r.hum})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void toBucketOrgTagColumnsArrayFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", new String[]{"location"},"return {\"hum\": r.hum}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucket: \"my-bucket\", org: \"my-org\", tagColumns: [\"location\"], fieldFn: (r) => return {\"hum\": r.hum})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void toBucketOrgTagColumnsCollectionFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", Arrays.asList("location", "host"),"return {\"hum\": r.hum}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucket: \"my-bucket\", org: \"my-org\", tagColumns: [\"location\", \"host\"], fieldFn: (r) => return {\"hum\": r.hum})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void toArray() {

        String[] tagColumns = {"location", "production"};
        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "example.com", "secret", "timestamp", tagColumns, "return {\"hum\": r.hum, \"temp\": r.temp}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucket: \"my-bucket\", org: \"my-org\", host: \"example.com\", token: \"secret\", timeColumn: \"timestamp\", tagColumns: [\"location\", \"production\"], fieldFn: (r) => return {\"hum\": r.hum, \"temp\": r.temp})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void toCollection() {

        List<String> tagColumns = Arrays.asList("location", "production");
        
        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "example.com", "secret", "timestamp", tagColumns, "return {\"hum\": r.hum, \"temp\": r.temp}");

        String expected = "from(bucket:\"telegraf\") |> "
                + "to(bucket: \"my-bucket\", org: \"my-org\", host: \"example.com\", token: \"secret\", timeColumn: \"timestamp\", tagColumns: [\"location\", \"production\"], fieldFn: (r) => return {\"hum\": r.hum, \"temp\": r.temp})";
        Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace(expected);
    }

}
