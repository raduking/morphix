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
package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TypeHolder}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypeHolderTest {

	@Test
	void shouldReturnTheSameCachedHolderForTheSameClass() {
		TypeHolder holder1 = TypeHolder.of(String.class);
		TypeHolder holder2 = TypeHolder.of(String.class);

		assertSame(holder1, holder2);
	}

	@Test
	void shouldReturnANewHolderForEachGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};

		TypeHolder holder1 = TypeHolder.of(genericType);
		TypeHolder holder2 = TypeHolder.of(genericType);

		assertNotSame(holder1, holder2);
	}

	@Test
	void shouldReturnNoneWhenClassIsNull() {
		assertSame(TypeHolder.NONE, TypeHolder.of((Class<?>) null));
	}

	@Test
	void shouldReturnClassTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		Class<String> classType = holder.getClassType();

		assertSame(String.class, classType);
	}

	@Test
	void shouldReturnNullClassTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		assertNull(holder.getClassType());
	}

	@Test
	void shouldReturnGenericTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		GenericClass<List<String>> result = holder.getGenericType();

		assertSame(genericType, result);
	}

	@Test
	void shouldReturnNullGenericTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertNull(holder.getGenericType());
	}

	@Test
	void shouldReturnTrueOnHasClassTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertTrue(holder.hasClassType());
	}

	@Test
	void shouldReturnFalseOnHasClassTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		assertFalse(holder.hasClassType());
	}

	@Test
	void shouldReturnTrueOnHasGenericTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		assertTrue(holder.hasGenericType());
	}

	@Test
	void shouldReturnFalseOnHasGenericTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertFalse(holder.hasGenericType());
	}

	@Test
	void shouldReturnTrueOnHasTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertTrue(holder.hasType());
	}

	@Test
	void shouldReturnTrueOnHasTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		assertTrue(holder.hasType());
	}

	@Test
	void shouldReturnFalseOnHasTypeWhenHolderHasNoType() {
		TypeHolder holder = TypeHolder.of((GenericClass<?>) null);

		assertFalse(holder.hasType());
	}

	@Test
	void shouldReturnClassAsTypeForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		Type type = holder.getType();

		assertSame(String.class, type);
	}

	@Test
	void shouldReturnCapturedTypeForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		Type type = holder.getType();

		assertSame(genericType.getType(), type);
	}

	@Test
	void shouldReturnNullTypeWhenHolderHasNoType() {
		TypeHolder holder = TypeHolder.of((GenericClass<?>) null);

		assertNull(holder.getType());
	}

	@Test
	void shouldReturnTypeNameForHolderBuiltFromClass() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertThat(holder.getTypeName(), equalTo(String.class.getTypeName()));
	}

	@Test
	void shouldReturnTypeNameForHolderBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeHolder holder = TypeHolder.of(genericType);

		assertThat(holder.getTypeName(), equalTo(genericType.getType().getTypeName()));
	}

	@Test
	void shouldReturnUnknownTypeNameWhenHolderHasNoType() {
		TypeHolder holder = TypeHolder.of((GenericClass<?>) null);

		assertThat(holder.getTypeName(), equalTo(TypeHolder.UNKNOWN_TYPE));
	}

	@Test
	void shouldReturnUnknownTypeNameWhenGenericTypeHasNoCapturedType() {
		GenericClass<?> genericType = GenericClass.of();

		TypeHolder holder = TypeHolder.of(genericType);

		assertTrue(holder.hasGenericType());
		assertThat(holder.getTypeName(), equalTo(TypeHolder.UNKNOWN_TYPE));
	}

	@Test
	void shouldHaveTheUnknownTypeConstant() {
		assertThat(TypeHolder.UNKNOWN_TYPE, equalTo("<unknown-type>"));
	}

	@Test
	void shouldHaveTheNoneConstantWithoutAnyType() {
		assertFalse(TypeHolder.NONE.hasClassType());
		assertFalse(TypeHolder.NONE.hasGenericType());
		assertFalse(TypeHolder.NONE.hasType());
		assertNull(TypeHolder.NONE.getClassType());
		assertNull(TypeHolder.NONE.getGenericType());
		assertNull(TypeHolder.NONE.getType());
		assertThat(TypeHolder.NONE.getTypeName(), equalTo(TypeHolder.UNKNOWN_TYPE));
	}

	@Test
	void shouldReturnTheSameHolderFromSafe() {
		TypeHolder holder = TypeHolder.of(String.class);

		assertSame(holder, TypeHolder.safe(holder));
	}

	@Test
	void shouldReturnNoneFromSafeWhenHolderIsNull() {
		assertSame(TypeHolder.NONE, TypeHolder.safe(null));
	}
}
