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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.VariableAssignment;
import com.influxdb.utils.Arguments;

/**
 * Merges two or more input streams into a single output stream..
 */
public final class UnionFlux extends AbstractParametrizedFlux {

    private final List<Flux> tables = new ArrayList<>();

    public UnionFlux() {
        super();
        withPropertyValue("tables", (Supplier<String>) () -> tables
                .stream().map(table -> table instanceof VariableAssignment
                        ? ((VariableAssignment) table).getVariableName()
                        : table.toString())
                .collect(Collectors.joining(",", "[", "]")));
    }


    @Nonnull
    @Override
    protected String operatorName() {
        return "union";
    }

    /**
     * @param tables Flux script to union
     * @return this
     */
    @Nonnull
    public UnionFlux withTables(@Nonnull final Flux... tables) {
        Arguments.checkNotNull(tables, "tables");
        this.tables.addAll(Arrays.asList(tables));
        return this;
    }


}
