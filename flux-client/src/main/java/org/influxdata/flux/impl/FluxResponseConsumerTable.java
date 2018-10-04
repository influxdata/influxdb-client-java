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
package org.influxdata.flux.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.flux.domain.FluxTable;
import org.influxdata.platform.rest.Cancellable;

/**
 * @author Jakub Bednar (bednar@github) (04/10/2018 10:59)
 */
class FluxResponseConsumerTable implements FluxCsvParser.FluxResponseConsumer {

    private List<FluxTable> tables = new ArrayList<>();

    @Override
    public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxTable table) {
        tables.add(index, table);
    }

    @Override
    public void accept(final int index, @Nonnull final Cancellable cancellable, @Nonnull final FluxRecord record) {
        tables.get(index).getRecords().add(record);
    }

    @Nonnull
    List<FluxTable> getTables() {
        return tables;
    }
}