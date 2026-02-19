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
package org.morphix.convert.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.morphix.convert.FieldHandlerResult.HANDLED;
import static org.morphix.reflection.ExtendedField.of;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.convert.FieldHandlerResult;

/**
 * Test class for {@link NullSourceSkipper}.
 *
 * @author Radu Sebastian LAZIN
 */
class NullSourceSkipperTest {

	public static class Source {
		Integer s;
	}

	public static class Destination {
		String s;
	}

	@Test
	void shouldReturnTrueOnConditionNoMatterTheArguments() {
		boolean result = new NullSourceSkipper().condition(null, null);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnBreakOnHandleForNullSource() throws Exception {
		Source src = new Source();
		Destination dst = new Destination();

		Field sField = Source.class.getDeclaredField("s");
		Field dField = Destination.class.getDeclaredField("s");

		FieldHandlerResult result = new NullSourceSkipper().handle(of(sField, src), of(dField, dst));

		assertThat(result, equalTo(HANDLED));
	}

	@Test
	void shouldReturnFalseWhenTestingEqualityWithNull() {
		boolean result = new NullSourceSkipper().equals(null);

		assertThat(result, equalTo(false));
	}
}
