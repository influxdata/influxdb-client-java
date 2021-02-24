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

import com.influxdb.query.dsl.AbstractFluxTest;
import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (10/10/2018 09:35)
 */
@RunWith(JUnitPlatform.class)
class ToFluxTest extends AbstractFluxTest {

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

        String expected = "from(bucket: v0) |> "
                + "to(bucketID: v1, orgID: v2, host: v3, token: v4, timeColumn: v5, tagColumns: v6, fieldFn: (r) => v7)";

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"O1\"",
                "v2", "\"02\"",
                "v3", "\"example.com\"",
                "v4", "\"secret\"",
                "v5", "\"timestamp\"",
                "v6", new String[]{"location", "production"},
                "v7", "return {\"hum\": r.hum, \"temp\": r.temp}"
        );
    }

    @Test
    void toBucket() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> to(bucket: v1)");

        assertVariables(query, "v0", "\"telegraf\"", "v1", "\"my-bucket\"");
    }

    @Test
    void toBucketOrg() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org");

        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux)
                .isEqualToIgnoringWhitespace("from(bucket: v0) |> to(bucket: v1, org: v2)");

        assertVariables(query, "v0", "\"telegraf\"", "v1", "\"my-bucket\"", "v2", "\"my-org\"");
    }

    @Test
    void toBucketOrgFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "return {\"hum\": r.hum}");

        String expected = "from(bucket: v0) |> "
                + "to(bucket: v1, org: v2, fieldFn: (r) => v3)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"my-bucket\"",
                "v2", "\"my-org\"",
                "v3", "return {\"hum\": r.hum}");
    }

    @Test
    void toBucketOrgTagColumnsArrayFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", new String[]{"location"}, "return {\"hum\": r.hum}");

        String expected = "from(bucket: v0) |> "
                + "to(bucket: v1, org: v2, tagColumns: v3, fieldFn: (r) => v4)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"my-bucket\"",
                "v2", "\"my-org\"",
                "v3", new String[]{"location"},
                "v4", "return {\"hum\": r.hum}");
    }

    @Test
    void toBucketOrgTagColumnsCollectionFieldFn() {

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", Arrays.asList("location", "host"), "return {\"hum\": r.hum}");

        String expected = "from(bucket: v0) |> "
                + "to(bucket: v1, org: v2, tagColumns: v3, fieldFn: (r) => v4)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"my-bucket\"",
                "v2", "\"my-org\"",
                "v3", Arrays.asList("location", "host"),
                "v4", "return {\"hum\": r.hum}");
    }

    @Test
    void toArray() {

        String[] tagColumns = {"location", "production"};
        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "example.com", "secret", "timestamp", tagColumns, "return {\"hum\": r.hum, \"temp\": r.temp}");

        String expected = "from(bucket: v0) |> "
                + "to(bucket: v1, org: v2, host: v3, token: v4, timeColumn: v5, tagColumns: v6, fieldFn: (r) => v7)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"my-bucket\"",
                "v2", "\"my-org\"",
                "v3", "\"example.com\"",
                "v4", "\"secret\"",
                "v5", "\"timestamp\"",
                "v6", tagColumns,
                "v7", "return {\"hum\": r.hum, \"temp\": r.temp}"
        );
    }

    @Test
    void toCollection() {

        List<String> tagColumns = Arrays.asList("location", "production");

        Flux flux = Flux
                .from("telegraf")
                .to("my-bucket", "my-org", "example.com", "secret", "timestamp", tagColumns, "return {\"hum\": r.hum, \"temp\": r.temp}");

        String expected = "from(bucket: v0) |> "
                + "to(bucket: v1, org: v2, host: v3, token: v4, timeColumn: v5, tagColumns: v6, fieldFn: (r) => v7)";
        Flux.Query query = flux.toQuery();
        Assertions.assertThat(query.flux).isEqualToIgnoringWhitespace(expected);
        assertVariables(query,
                "v0", "\"telegraf\"",
                "v1", "\"my-bucket\"",
                "v2", "\"my-org\"",
                "v3", "\"example.com\"",
                "v4", "\"secret\"",
                "v5", "\"timestamp\"",
                "v6", tagColumns,
                "v7", "return {\"hum\": r.hum, \"temp\": r.temp}"
        );
    }
}
