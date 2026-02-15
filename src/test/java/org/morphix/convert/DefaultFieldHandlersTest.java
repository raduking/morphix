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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link DefaultFieldHandlers}.
 *
 * @author Radu Sebastian LAZIN
 */
class DefaultFieldHandlersTest {

	static final FieldHandler[] FIELD_HANDLERS = {
			DefaultFieldHandlers.FIELD_HANDLER_NULL_SOURCE_SKIPPER,
			DefaultFieldHandlers.FIELD_HANDLER_STATIC_FIELD_SKIPPER,
			DefaultFieldHandlers.FIELD_HANDLER_DIRECT_ASSIGNMENT,
			DefaultFieldHandlers.FIELD_HANDLER_PRIMITIVE_ASSIGNMENT,
			DefaultFieldHandlers.FIELD_HANDLER_NUMBER_TO_NUMBER,
			DefaultFieldHandlers.FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_STRING,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_CHAR_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_CHAR_SEQUENCE_TO_ANY,
			DefaultFieldHandlers.FIELD_HANDLER_ITERABLE_TO_ITERABLE,
			DefaultFieldHandlers.FIELD_HANDLER_ARRAY_TO_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_ITERABLE_TO_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_ARRAY_TO_ITERABLE,
			DefaultFieldHandlers.FIELD_HANDLER_MAP_TO_MAP,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY_FROM_STATIC_METHOD,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY_FROM_CONSTRUCTOR,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ITERABLE,
			DefaultFieldHandlers.FIELD_HANDLER_MAP_TO_ANY,
	};

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<DefaultFieldHandlers> defaultConstructor = DefaultFieldHandlers.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<DefaultFieldHandlers>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldKeepTheHandlersOrder() {
		for (int i = 0; i < DefaultFieldHandlers.FIELD_HANDLERS_CHAIN.length; ++i) {
			assertThat(FIELD_HANDLERS[i], equalTo(DefaultFieldHandlers.FIELD_HANDLERS_CHAIN[i]));
		}
	}

	@Test
	void shouldReturnTheDefaultFieldHandlersOnList() {
		List<FieldHandler> result = DefaultFieldHandlers.list();

		assertThat(result, hasSize(FIELD_HANDLERS.length));
		assertThat(result, equalTo(DefaultFieldHandlers.FIELD_HANDLERS_LIST));
	}

}
