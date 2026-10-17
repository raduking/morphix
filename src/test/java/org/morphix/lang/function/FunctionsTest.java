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
package org.morphix.lang.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.utils.Tests;

/**
 * Test class for {@link Functions}.
 *
 * @author Radu Sebastian LAZIN
 */
class FunctionsTest {

	@Test
	void shouldThrowExceptionWhenTryingToInstantiate() {
		UnsupportedOperationException e = Tests.verifyDefaultConstructorThrows(Functions.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldAlwaysReturnTrueOnReturnTrueAndBeSingleton() {
		Function<Object, Boolean> f1 = Functions.returnTrue();
		Function<String, Boolean> f2 = Functions.returnTrue();

		assertSame(f1, f2);

		assertThat(f1.apply(new Object()), equalTo(true));
		assertThat(f1.apply("test"), equalTo(true));
		assertThat(f1.apply(42), equalTo(true));
		assertThat(f1.apply(null), equalTo(true));
	}

	@Test
	void shouldAlwaysReturnFalseOnReturnFalseAndBeSingleton() {
		Function<Object, Boolean> f1 = Functions.returnFalse();
		Function<String, Boolean> f2 = Functions.returnFalse();

		assertSame(f1, f2);

		assertThat(f1.apply(new Object()), equalTo(false));
		assertThat(f1.apply("test"), equalTo(false));
		assertThat(f1.apply(42), equalTo(false));
		assertThat(f1.apply(null), equalTo(false));
	}

	@Test
	void shouldAlwaysReturnNullOnReturnNullAndBeSingleton() {
		Function<String, Integer> f1 = Functions.returnNull();
		Function<Object, Object> f2 = Functions.returnNull();

		assertSame(f1, f2);

		assertThat(f1.apply("test"), nullValue());
		assertThat(f1.apply(null), nullValue());
		assertThat(f2.apply(new Object()), nullValue());
	}

}
