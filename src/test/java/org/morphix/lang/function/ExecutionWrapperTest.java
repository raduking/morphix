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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExecutionWrapper}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExecutionWrapperTest {

	private static final String HELLO_WORLD = "Hello, World!";
	private static final String PREFIX = "Message: ";

	@Test
	void shouldWrapSupplier() {
		ExecutionWrapper<String> wrapper = supplier -> () -> wrapWithPrefix(PREFIX, supplier);
		String result = wrapper.execute(() -> HELLO_WORLD);

		assertThat(result, equalTo(PREFIX + HELLO_WORLD));
	}

	@Test
	void shouldWrapRunnable() {
		StringBuilder builder = new StringBuilder();
		ExecutionWrapper<Void> wrapper = supplier -> () -> {
			builder.append(PREFIX);
			return supplier.get();
		};
		wrapper.execute(() -> builder.append(HELLO_WORLD));

		assertThat(builder.toString(), equalTo(PREFIX + HELLO_WORLD));
	}

	@Test
	void shouldChainWrappers() {
		ExecutionWrapper<String> wrapper1 = supplier -> () -> wrapWithPrefix("First: ", supplier);
		ExecutionWrapper<String> wrapper2 = supplier -> () -> wrapWithPrefix("Second: ", supplier);
		ExecutionWrapper<String> chainedWrapper = wrapper1.andThen(wrapper2);

		String result = chainedWrapper.execute(() -> HELLO_WORLD);

		assertThat(result, equalTo("First: Second: " + HELLO_WORLD));
	}

	@Test
	void shouldComposeWrappers() {
		ExecutionWrapper<String> wrapper1 = supplier -> () -> wrapWithPrefix("First: ", supplier);
		ExecutionWrapper<String> wrapper2 = supplier -> () -> wrapWithPrefix("Second: ", supplier);
		ExecutionWrapper<String> chainedWrapper = wrapper1.compose(wrapper2);

		String result = chainedWrapper.execute(() -> HELLO_WORLD);

		assertThat(result, equalTo("Second: First: " + HELLO_WORLD));
	}

	@Test
	void shouldUseIdentityWrapper() {
		ExecutionWrapper<String> identityWrapper = ExecutionWrapper.identity();

		String result = identityWrapper.execute(() -> HELLO_WORLD);

		assertThat(result, equalTo(HELLO_WORLD));
	}

	@Test
	void shouldUseAroundWrapper() {
		StringBuilder builder = new StringBuilder();

		ExecutionWrapper<String> aroundWrapper = ExecutionWrapper.around(
				() -> builder.append("Before: "),
				() -> builder.append(" After"));

		String result = aroundWrapper.execute(() -> {
			builder.append(HELLO_WORLD);
			return HELLO_WORLD;
		});

		assertThat(result, equalTo(HELLO_WORLD));
		assertThat(builder.toString(), equalTo("Before: " + HELLO_WORLD + " After"));
	}

	private static String wrapWithPrefix(final String prefix, final Supplier<String> supplier) {
		String value = supplier.get();
		return prefix + value;
	}
}
