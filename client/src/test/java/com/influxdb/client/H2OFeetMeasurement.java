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

import java.time.Instant;
import javax.annotation.Nullable;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

/**
 * @author Jakub Bednar (bednar@github) (17/10/2018 13:46)
 */
@Measurement(name = "h2o")
public class H2OFeetMeasurement {

    @Column(name = "location", tag = true)
    String location;

    @Column(name = "water_level")
    Double level;

    @Column(name = "level description")
    String description;

    @Column(name = "time", timestamp = true)
    Instant time;

    public H2OFeetMeasurement() {
    }

    H2OFeetMeasurement(String location, Double level, String description, @Nullable final Long millis) {
        this(location, level, description, millis != null ? Instant.ofEpochMilli(millis) : null);
    }

    H2OFeetMeasurement(String location, Double level, String description, @Nullable final Instant time) {
        this.location = location;
        this.level = level;
        this.description = description;
        this.time = time;
    }
}