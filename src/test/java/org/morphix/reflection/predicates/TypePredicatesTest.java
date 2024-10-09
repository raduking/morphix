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
package org.morphix.reflection.predicates;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.morphix.lang.function.Predicates.not;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.morphix.convert.Conversions;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link TypePredicates}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypePredicatesTest {

	@Test
	void shouldTestCollectionsPredicates() {
		boolean result = TypePredicates.isQueue().test(Queue.class);
		assertThat(result, equalTo(true));

		result = TypePredicates.isSet().test(Set.class);
		assertThat(result, equalTo(true));
	}

	@Test
	void shouldThrowExceptionIfThisClassIsInstantiatedWithDefaultConstructor() throws Exception {
		Throwable targetException = null;
		Constructor<TypePredicates> defaultConstructor = TypePredicates.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<TypePredicates>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldCombinePredicatesForArrays() {
		Type type = Integer[].class;
		boolean result = TypePredicates.isArray().test(type);

		assertThat(result, equalTo(true));

		type = Integer.class;
		result = TypePredicates.isArray().test(type);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldCombinePredicatesForCharSequence() {
		Type type = String.class;
		boolean result = TypePredicates.isCharSequence().test(type);

		assertThat(result, equalTo(true));

		type = Integer.class;
		result = TypePredicates.isCharSequence().test(type);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldCombinePredicatesForSrc() {
		Type type = Conversions.class;
		Predicate<Type> predicate = TypePredicates.isAClassAnd(not(ClassPredicates.isA(Conversions.class)));
		boolean result = predicate.test(type);

		assertThat(result, equalTo(false));

		predicate = TypePredicates.isAClass();
		result = predicate.test(type);

		assertThat(result, equalTo(true));

		type = Type.class;
		result = predicate.test(type);

		assertThat(result, equalTo(true));
	}

}
