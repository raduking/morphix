/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.convert.function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ConvertFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConvertFunctionTest {

	static class Src {

		private String value;

		public Src(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	static class Dst {

		private Integer value;

		public Integer getValue() {
			return value;
		}

		public void setValue(final Integer value) {
			this.value = value;
		}
	}

	@Test
	void shouldConvertWithConvertFunction() {
		ConvertFunction<Src, Dst> function = (s, d) -> d.setValue(Integer.valueOf(s.getValue()));

		Src src = new Src("123");
		Dst dst = new Dst();
		function.accept(src, dst);

		assertThat(dst.getValue(), equalTo(123));
	}
}
