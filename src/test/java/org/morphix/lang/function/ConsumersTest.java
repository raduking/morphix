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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.reflection.ReflectionException;

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

	@Test
	void shouldBiConsumeNothing() {
		BiConsumer<?, ?> biConsumer = Fields.IgnoreAccess.getStatic(Consumers.class, "EMPTY_BI_CONSUMER");

		BiConsumer<?, ?> emptyBiConsumer = Consumers.noBiConsumer();

		assertThat(biConsumer, equalTo(emptyBiConsumer));
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

	@Test
	void shouldReturnEmptyBiConsumerThatDoesNothing() {
		BiConsumer<A, A> consumer = Consumers.noBiConsumer();

		A a = mock(A.class);
		consumer.accept(a, a);

		verifyNoInteractions(a);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateClass() {
		ReflectionException reflectionException = assertThrows(ReflectionException.class, () -> Constructors.IgnoreAccess.newInstance(Consumers.class));
		InvocationTargetException invocationTargetException = JavaObjects.cast(reflectionException.getCause());
		UnsupportedOperationException unsupportedOperationException = JavaObjects.cast(invocationTargetException.getCause());
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

}
