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
package com.influxdb.spring.metrics;

import io.micrometer.influx2.Influx2Config;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;

/**
 * Adapter to convert {@link Influx2Properties} to an {@link Influx2Config}.
 *
 * @author Jakub Bednar (bednar@github) (06/05/2019 13:29)
 */
class Influx2PropertiesConfigAdapter extends
        StepRegistryPropertiesConfigAdapter<Influx2Properties> implements Influx2Config {

    Influx2PropertiesConfigAdapter(final Influx2Properties properties) {
        super(properties);
    }

    @Override
    public String bucket() {
        return get(Influx2Properties::getBucket, Influx2Config.super::bucket);
    }

    @Override
    public String org() {
        return get(Influx2Properties::getOrg, Influx2Config.super::org);
    }

    @Override
    public String token() {
        return get(Influx2Properties::getToken, Influx2Config.super::token);
    }

    @Override
    public String uri() {
        return get(Influx2Properties::getUri, Influx2Config.super::uri);
    }

    @Override
    public boolean compressed() {
        return get(Influx2Properties::isCompressed, Influx2Config.super::compressed);
    }

    @Override
    public boolean autoCreateBucket() {
        return get(Influx2Properties::isAutoCreateBucket, Influx2Config.super::autoCreateBucket);
    }

    @Override
    public Integer everySeconds() {
        return get(Influx2Properties::getEverySeconds, Influx2Config.super::everySeconds);
    }
        }