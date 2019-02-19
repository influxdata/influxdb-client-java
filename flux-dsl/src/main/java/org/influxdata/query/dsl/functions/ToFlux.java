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
package org.influxdata.query.dsl.functions;

import java.util.Collection;
import javax.annotation.Nonnull;

import org.influxdata.Arguments;
import org.influxdata.query.dsl.Flux;

/**
 * The To operation takes data from a stream and writes it to a bucket.
 * <a href="http://bit.ly/flux-spec#to">See SPEC</a>.
 *
 * <h3>Options</h3>
 * <ul>
 *     <li><b>bucket</b> - The bucket to which data will be written. [string]</li>
 *     <li><b>bucketID</b> - The bucket to which data will be written. [string]</li>
 *     <li><b>org</b> - The organization name of the above bucket. [string]</li>
 *     <li><b>orgID</b> - The organization ID of the above bucket. [string]</li>
 *     <li><b>host</b> - The remote host to write to. [string]</li>
 *     <li><b>token</b> - The authorization token to use when writing to a remote host. [string]</li>
 *     <li><b>timeColumn</b> - The time column of the output. [string]</li>
 *     <li><b>tagColumns</b> - The tag columns of the output.</li>
 *     <li><b>fieldFn</b> - Function that takes a record from the input table and returns an object.</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .to("my-bucket", "my-org");
 * </pre>
 *
 * @author Jakub Bednar (10/10/2018 07:58)
 */
public final class ToFlux extends AbstractParametrizedFlux {

    public ToFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "to";
    }

    /**
     * @param bucket The bucket to which data will be written.
     * @return this
     */
    @Nonnull
    public ToFlux withBucket(@Nonnull final String bucket) {

        Arguments.checkNonEmpty(bucket, "bucket");

        this.withPropertyValueEscaped("bucket", bucket);

        return this;
    }

    /**
     * @param bucketID The ID of the bucket to which data will be written.
     * @return this
     */
    @Nonnull
    public ToFlux withBucketID(@Nonnull final String bucketID) {

        Arguments.checkNonEmpty(bucketID, "bucketID");

        this.withPropertyValueEscaped("bucketID", bucketID);

        return this;
    }

    /**
     * @param org The organization name of the above bucket.
     * @return this
     */
    @Nonnull
    public ToFlux withOrg(@Nonnull final String org) {

        Arguments.checkNonEmpty(org, "org");

        this.withPropertyValueEscaped("org", org);

        return this;
    }

    /**
     * @param orgID The organization name of the above bucket.
     * @return this
     */
    @Nonnull
    public ToFlux withOrgID(@Nonnull final String orgID) {

        Arguments.checkNonEmpty(orgID, "orgID");

        this.withPropertyValueEscaped("orgID", orgID);

        return this;
    }

    /**
     * @param host The remote host to write to.
     * @return this
     */
    @Nonnull
    public ToFlux withHost(@Nonnull final String host) {

        Arguments.checkNonEmpty(host, "host");

        this.withPropertyValueEscaped("host", host);

        return this;
    }

    /**
     * @param token The authorization token to use when writing to a remote host.
     * @return this
     */
    @Nonnull
    public ToFlux withToken(@Nonnull final String token) {

        Arguments.checkNonEmpty(token, "token");

        this.withPropertyValueEscaped("token", token);

        return this;
    }

    /**
     * @param timeColumn The time column of the output.
     * @return this
     */
    @Nonnull
    public ToFlux withTimeColumn(@Nonnull final String timeColumn) {

        Arguments.checkNonEmpty(timeColumn, "timeColumn");

        this.withPropertyValueEscaped("timeColumn", timeColumn);

        return this;
    }

    /**
     * @param tagColumns The tag columns of the output.
     * @return this
     */
    @Nonnull
    public ToFlux withTagColumns(@Nonnull final String[] tagColumns) {

        Arguments.checkNotNull(tagColumns, "tagColumns");

        this.withPropertyValue("tagColumns", tagColumns);

        return this;
    }

    /**
     * @param tagColumns The tag columns of the output.
     * @return this
     */
    @Nonnull
    public ToFlux withTagColumns(@Nonnull final Collection<String> tagColumns) {

        Arguments.checkNotNull(tagColumns, "tagColumns");

        this.withPropertyValue("tagColumns", tagColumns);

        return this;
    }

    /**
     * @param fieldFn Function that takes a record from the input table and returns an object.
     * @return this
     */
    @Nonnull
    public ToFlux withFieldFunction(@Nonnull final String fieldFn) {

        Arguments.checkNonEmpty(fieldFn, "fieldFn");

        this.withFunction("fieldFn: (r)", fieldFn);

        return this;
    }
}
