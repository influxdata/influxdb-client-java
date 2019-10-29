/*
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
package com.influxdb.client.internal;

import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.Arguments;
import com.influxdb.client.DeleteApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.service.DeleteService;
import com.influxdb.internal.AbstractRestClient;

import retrofit2.Call;

/**
 * @author Pavlina Rolincova (rolincova@github) (25/10/2019).
 */
public class DeleteApiImpl extends AbstractRestClient implements DeleteApi {

    private static final Logger LOG = Logger.getLogger(DeleteApiImpl.class.getName());

    private final DeleteService service;

    DeleteApiImpl(DeleteService service) {

        Arguments.checkNotNull(service, "service");

        this.service = service;
    }

    @Override
    public void delete(@Nonnull OffsetDateTime start,
                       @Nonnull OffsetDateTime stop,
                       @Nonnull String predicate,
                       @Nonnull String bucket,
                       @Nonnull String organization) {

        Arguments.checkNotNull(start, "Start is required");
        Arguments.checkNotNull(stop, "Stop is required");
        Arguments.checkNotNull(predicate, "Predicate is required");
        Arguments.checkNotNull(bucket, "Bucket is required");
        Arguments.checkNotNull(organization, "Organization is required");

        DeletePredicateRequest request = new DeletePredicateRequest();
        request.setStart(start);
        request.setStop(stop);
        request.setPredicate(predicate);

        delete(request, bucket, organization);
    }

    @Override
    public void delete(@Nonnull DeletePredicateRequest predicate,
                       @Nonnull String bucket,
                       @Nonnull String organization) {

        Arguments.checkNotNull(predicate, "Predicate is required");
        Arguments.checkNotNull(bucket, "Bucket is required");
        Arguments.checkNotNull(organization, "Organization is required");

        LOG.log(Level.FINEST,
                "Deleting time-series data from InfluxDB (org={0}, bucket={1})...",
                new Object[]{organization, bucket});

        Call<Void> call = service.deletePost(predicate, null, organization, bucket,
                null, null);

        execute(call);

        LOG.log(Level.FINEST, "Data was deleted from InfluxDB: (org={0}, bucket={1})",
                new Object[]{organization, bucket});
    }
}
