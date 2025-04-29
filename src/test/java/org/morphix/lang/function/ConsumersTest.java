/*
 * Copyright 2025 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link Consumers}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConsumersTest {

	@Test
	void shouldConsumeNothing() {
		Consumer<?> consumer = Fields.IgnoreAccess.getStatic(Consumers.class, "EMPTY_CONSUMER");

		Consumer<?> emptyConsumer = Consumers.noConsumer();

		assertThat(emptyConsumer, equalTo(Consumers.consumeNothing()));
		assertThat(consumer, equalTo(emptyConsumer));
	}

	static class A {
		// empty
	}

	@Test
	void shouldReturnEmptyConsumerThatDoesNothing() {
		Consumer<A> consumer = Consumers.consumeNothing();

		A a = mock(A.class);
		consumer.accept(a);

		verifyNoInteractions(a);
	}

}
