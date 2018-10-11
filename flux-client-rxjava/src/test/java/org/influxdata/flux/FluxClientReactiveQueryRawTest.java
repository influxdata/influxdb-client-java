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
package org.influxdata.flux;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (03/08/2018 14:40)
 */
@RunWith(JUnitPlatform.class)
class FluxClientReactiveQueryRawTest extends AbstractFluxClientReactiveTest {

    @Test
    void queryRaw() {

        mockServer.enqueue(createResponse());

        Flowable<String> result = fluxClient.queryRaw("from(bucket:\"telegraf\")");

        assertResult(result);
    }

    @Test
    void queryPublisher() {

        mockServer.enqueue(createResponse());

        Flowable<String> result = fluxClient.queryRaw(Flowable.just("from(bucket:\"telegraf\")"));

        assertResult(result);
    }

    private void assertResult(@Nonnull  final Flowable<String> result) {
        result
                .test()
                .assertValueCount(8)
                .assertValueAt(0, "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,string,string,double")
                .assertValueAt(1, ",result,table,_start,_stop,_time,region,host,_value")
                .assertValueAt(2, ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:00Z,east,A,15.43")
                .assertValueAt(3, ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:20Z,east,B,59.25")
                .assertValueAt(4, ",mean,0,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:40Z,east,C,52.62")
                .assertValueAt(5, ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:00Z,west,A,62.73")
                .assertValueAt(6, ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:20Z,west,B,12.83")
                .assertValueAt(7, ",mean,1,2018-05-08T20:50:00Z,2018-05-08T20:51:00Z,2018-05-08T20:50:40Z,west,C,51.62");
    }

}