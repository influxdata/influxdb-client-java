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
package org.influxdata.flux.functions;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.Flux;
import org.influxdata.platform.Arguments;

/**
 * Abstract base class for operators that take an upstream source of {@link Flux}.
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 07:29)
 */
abstract class AbstractFluxWithUpstream extends Flux {

    @Nullable
    Flux source;

    AbstractFluxWithUpstream() {
    }

    AbstractFluxWithUpstream(@Nonnull final Flux source) {

        Arguments.checkNotNull(source, "Source is required");

        this.source = source;
    }

    @Override
    public void appendActual(@Nonnull final Map<String, Object> parameters, @Nonnull final StringBuilder builder) {

        if (source != null) {
            source.appendActual(parameters, builder);
        }
    }

    /**
     * Append delimiter to Flux query.
     *
     * @param builder Flux query chain.
     */
    void appendDelimiter(@Nonnull final StringBuilder builder) {
        if (builder.length() != 0) {
            builder.append("\n");
            builder.append("\t|> ");
        }
    }
}
