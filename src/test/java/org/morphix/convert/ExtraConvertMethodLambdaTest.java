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
package org.morphix.convert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.Conversions.convertFrom;

import org.junit.jupiter.api.Test;

/**
 * Tests extra conversion lambdas.
 *
 * @author Radu Sebastian LAZIN
 */
class ExtraConvertMethodLambdaTest {

	public static class Source {
		Long testLong1;
		Long testLong2;
	}

	public static class Destination {
		Long testLong;
	}

	@Test
	void shouldConvertExtraFields() {
		Source s = new Source();
		s.testLong1 = 17L;
		s.testLong2 = 19L;

		Long expected = s.testLong1 + s.testLong2;

		Destination d = convertFrom(s, Destination::new, (src, dst) -> {
			dst.testLong = src.testLong1 + src.testLong2;
		});

		assertThat(d.testLong, equalTo(expected));
	}

}
