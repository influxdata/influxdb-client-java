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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.influxdata.flux.option.FluxConnectionOptions;
import org.influxdata.platform.AbstractTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 08:20)
 */
public abstract class AbstractITFluxClientReactive extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxClientReactive.class.getName());

    static final String DATABASE_NAME = "flux_react_database";

    FluxClientReactive fluxClient;

    @BeforeEach
    protected void setUp() {

        String influxURL = getInfluxDbURL();

        LOG.log(Level.FINEST, "Influx URL: {0}", influxURL);

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url(influxURL)
                .build();

        fluxClient = FluxClientReactiveFactory.connect(options);

        influxDBQuery("CREATE DATABASE " + DATABASE_NAME, DATABASE_NAME);
    }

    @AfterEach
    protected void after() {

        influxDBQuery("DROP DATABASE " + DATABASE_NAME, DATABASE_NAME);
    }
}