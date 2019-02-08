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
package org.influxdata.client.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (30/01/2019 08:45)
 */
@RunWith(JUnitPlatform.class)
class PageLinksTest {

    @Test
    void mappingLinks() {

        PageLinks pageLinks = new PageLinks();
        pageLinks.getLinks().put("self", "/api/v2/buckets?descending=false\u0026limit=20\u0026offset=0");

        Assertions.assertThat(pageLinks.getPrevPage()).isNull();
        Assertions.assertThat(pageLinks.getNextPage()).isNull();
        Assertions.assertThat(pageLinks.getSelfPage()).isNotNull();
        Assertions.assertThat(pageLinks.getSelfPage().getLimit()).isEqualTo(20);
        Assertions.assertThat(pageLinks.getSelfPage().getDescending()).isFalse();
        Assertions.assertThat(pageLinks.getSelfPage().getOffset()).isEqualTo(0);
        Assertions.assertThat(pageLinks.getSelfPage().getSortBy()).isNull();
    }

    @Test
    void sortBy() {

        PageLinks pageLinks = new PageLinks();
        pageLinks.getLinks().put("next", "/api/v2/buckets?descending=true\u0026limit=20\u0026offset=10\u0026sortBy=ID");

        Assertions.assertThat(pageLinks.getSelfPage()).isNull();
        Assertions.assertThat(pageLinks.getPrevPage()).isNull();
        Assertions.assertThat(pageLinks.getNextPage()).isNotNull();
        Assertions.assertThat(pageLinks.getNextPage().getLimit()).isEqualTo(20);
        Assertions.assertThat(pageLinks.getNextPage().getDescending()).isTrue();
        Assertions.assertThat(pageLinks.getNextPage().getOffset()).isEqualTo(10);
        Assertions.assertThat(pageLinks.getNextPage().getSortBy()).isEqualTo("ID");
    }

    @Test
    void isNotFindOptions() {

        PageLinks pageLinks = new PageLinks();
        pageLinks.getLinks().put("next", "/api/v2/buckets?descendin=true\u0026limi=20\u0026offse=10\u0026sortB=ID");

        Assertions.assertThat(pageLinks.getSelfPage()).isNull();
        Assertions.assertThat(pageLinks.getPrevPage()).isNull();
        Assertions.assertThat(pageLinks.getNextPage()).isNull();
    }

    private class PageLinks extends AbstractPageLinks
    {
    }
}