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

import java.util.HashMap;

import com.influxdb.query.dsl.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (14/12/2020 10:24)
 */
class TailFluxTest
{
	@Test
	void tail() {

		Flux flux = Flux
				.from("telegraf")
				.tail(5);

		Assertions.assertThat(flux.toString()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> tail(n: 5)");
	}

	@Test
	void tailOffset() {

		Flux flux = Flux
				.from("telegraf")
				.tail(100, 10);

		Assertions.assertThat(flux.toString())
				.isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> tail(n: 100, offset:10)");
	}

	@Test
	void tailOffsetZero() {

		Flux flux = Flux
				.from("telegraf")
				.tail(100, 0);

		Assertions.assertThat(flux.toString())
				.isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> tail(n: 100, offset:0)");
	}

	@Test
	void tailPositive() {
		Assertions.assertThatThrownBy(() -> Flux.from("telegraf").tail(-5))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Expecting a positive number for Number of results");
	}

	@Test
	void tailByParameter() {

		Flux flux = Flux
				.from("telegraf")
				.tail()
				.withPropertyNamed("n", "tail");

		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("tail", 15);

		Assertions.assertThat(flux.toString(parameters))
				.isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> tail(n: 15)");
	}

	@Test
	void tailByParameterMissing() {

		Assertions.assertThatThrownBy(() -> Flux.from("telegraf").tail().withPropertyNamed("tail").toString())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("The parameter 'tail' is not defined.");
	}
}
