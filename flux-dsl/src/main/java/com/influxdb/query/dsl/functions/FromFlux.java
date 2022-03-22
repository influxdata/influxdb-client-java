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

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.utils.Arguments;

/**
 * From produces a stream of tables from the specified bucket.
 * <a href="http://bit.ly/flux-spec#from">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>bucket</b> - The name of the bucket to query [string]</li>
 * <li><b>hosts</b> - array of strings from(bucket:"telegraf", hosts:["host1", "host2"])</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux.from("telegraf");
 *
 * Flux flux = Flux
 *      .from("telegraf", new String[]{"192.168.1.200", "192.168.1.100"})
 *      .last();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:20)
 */
public final class FromFlux extends AbstractParametrizedFlux {

    private final FluxOptions options = new FluxOptions();

    public FromFlux() {
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "from";
    }


    /**
     * @param bucket Bucket name
     * @return {@link FromFlux}
     */
    @Nonnull
    public FromFlux withBucket(@Nonnull final String bucket) {

        Arguments.checkNonEmpty(bucket, "Bucket name");

        this.withPropertyValueEscaped("bucket", bucket);

        return this;
    }

    /**
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public FromFlux withHosts(@Nonnull final Collection<String> hosts) {

        Arguments.checkNotNull(hosts, "Hosts are required");

        this.withPropertyValue("hosts", hosts);

        return this;
    }

    /**
     * @param hosts the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public FromFlux withHosts(@Nonnull final String[] hosts) {

        Arguments.checkNotNull(hosts, "Hosts are required");

        this.withPropertyValue("hosts", hosts);

        return this;
    }

    /**
     * Use a timezone based on a location name.
     *
     * @param name Location name as defined by InfluxDB server operating system timezone database
     * @return this
     */
    @Nonnull
    public Flux withLocationNamed(@Nonnull final String name) {
        Arguments.checkNonEmpty(name, "name");
        this.options.location = String.format("timezone.location(name: \"%s\")", name);
        return this;
    }

    /**
     * Use a location with a fixed offset.
     *
     * @param offset The fixed duration for the location offset. The duration is the offset from UTC time.
     * @return this
     */
    @Nonnull
    public Flux withLocationFixed(@Nonnull final String offset) {
        Arguments.checkDuration(offset, "offset");
        this.options.location = String.format("timezone.fixed(offset: %s)", offset);
        return this;
    }

    @Override
    public void appendActual(@Nonnull final Map<String, Object> parameters, @Nonnull final StringBuilder builder) {
        // append timezone configuration
        if (options.location != null) {
            builder.append("import \"timezone\"");
            builder.append("\n");
            builder.append("option location = ");
            builder.append(options.location);
            builder.append("\n");
        }
        super.appendActual(parameters, builder);
    }

    @Override
    void appendDelimiter(@Nonnull final StringBuilder builder) {
    }

    private static class FluxOptions {
        private String location;
    }
}
