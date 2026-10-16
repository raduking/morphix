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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TypeDescriptor}.
 *
 * @author Radu Sebastian LAZIN
 */
class TypeDescriptorTest {

	@Test
	void shouldReturnTheSameCachedDescriptorForTheSameClass() {
		TypeDescriptor descriptor1 = TypeDescriptor.of(String.class);
		TypeDescriptor descriptor2 = TypeDescriptor.of(String.class);

		assertSame(descriptor1, descriptor2);
	}

	@Test
	void shouldReturnANewDescriptorForEachGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};

		TypeDescriptor descriptor1 = TypeDescriptor.of(genericType);
		TypeDescriptor descriptor2 = TypeDescriptor.of(genericType);

		assertNotSame(descriptor1, descriptor2);
	}

	@Test
	void shouldThrowNullPointerExceptionWhenClassIsNull() {
		assertThrows(NullPointerException.class, () -> TypeDescriptor.of((Class<?>) null));
	}

	@Test
	void shouldReturnClassTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		Class<String> classType = descriptor.getClassType();

		assertSame(String.class, classType);
	}

	@Test
	void shouldReturnNullClassTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertNull(descriptor.getClassType());
	}

	@Test
	void shouldReturnGenericTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		GenericClass<List<String>> result = descriptor.getGenericType();

		assertSame(genericType, result);
	}

	@Test
	void shouldReturnNullGenericTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		assertNull(descriptor.getGenericType());
	}

	@Test
	void shouldReturnTrueOnHasClassTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		assertTrue(descriptor.hasClassType());
	}

	@Test
	void shouldReturnFalseOnHasClassTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertFalse(descriptor.hasClassType());
	}

	@Test
	void shouldReturnTrueOnHasGenericTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertTrue(descriptor.hasGenericType());
	}

	@Test
	void shouldReturnFalseOnHasGenericTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		assertFalse(descriptor.hasGenericType());
	}

	@Test
	void shouldReturnTrueOnHasTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		assertTrue(descriptor.hasType());
	}

	@Test
	void shouldReturnTrueOnHasTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertTrue(descriptor.hasType());
	}

	@Test
	void shouldReturnFalseOnHasTypeWhenDescriptorHasNoType() {
		TypeDescriptor descriptor = TypeDescriptor.of((GenericClass<?>) null);

		assertFalse(descriptor.hasType());
	}

	@Test
	void shouldReturnClassAsTypeForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		Type type = descriptor.getType();

		assertSame(String.class, type);
	}

	@Test
	void shouldReturnCapturedTypeForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		Type type = descriptor.getType();

		assertSame(genericType.getType(), type);
	}

	@Test
	void shouldReturnNullTypeWhenDescriptorHasNoType() {
		TypeDescriptor descriptor = TypeDescriptor.of((GenericClass<?>) null);

		assertNull(descriptor.getType());
	}

	@Test
	void shouldReturnTypeNameForDescriptorBuiltFromClass() {
		TypeDescriptor descriptor = TypeDescriptor.of(String.class);

		assertThat(descriptor.getTypeName(), equalTo(String.class.getTypeName()));
	}

	@Test
	void shouldReturnTypeNameForDescriptorBuiltFromGenericType() {
		GenericClass<List<String>> genericType = new GenericClass<>() {
			// empty
		};
		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertThat(descriptor.getTypeName(), equalTo(genericType.getType().getTypeName()));
	}

	@Test
	void shouldReturnUnknownTypeNameWhenDescriptorHasNoType() {
		TypeDescriptor descriptor = TypeDescriptor.of((GenericClass<?>) null);

		assertThat(descriptor.getTypeName(), equalTo(TypeDescriptor.UNKNOWN_TYPE));
	}

	@Test
	void shouldReturnUnknownTypeNameWhenGenericTypeHasNoCapturedType() {
		GenericClass<?> genericType = GenericClass.of();

		TypeDescriptor descriptor = TypeDescriptor.of(genericType);

		assertTrue(descriptor.hasGenericType());
		assertThat(descriptor.getTypeName(), equalTo(TypeDescriptor.UNKNOWN_TYPE));
	}

	@Test
	void shouldHaveTheUnknownTypeConstant() {
		assertThat(TypeDescriptor.UNKNOWN_TYPE, equalTo("<unknown-type>"));
	}
}
