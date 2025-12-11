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
package org.morphix.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.ReflectionException;

/**
 * Test class for {@link Enums}.
 *
 * @author RaduSebastian LAZIN
 */
class EnumsTest {

	private static final String INVALID_VALUE = "INVALID_VALUE";

	enum ExampleEnum {
		A,
		B,
		C
	}

	@Test
	void shouldBuildNameMap() {
		ExampleEnum[] values = ExampleEnum.values();
		Map<String, ExampleEnum> nameMap = Enums.buildNameMap(values);

		assertThat(nameMap.size(), equalTo(values.length));

		for (ExampleEnum exampleEnum : values) {
			ExampleEnum expected = nameMap.get(exampleEnum.name());
			assertThat(exampleEnum, equalTo(expected));
		}
	}

	@Test
	void shouldThrowExceptionForInvalidValues() {
		ExampleEnum[] values = ExampleEnum.values();
		Map<String, ExampleEnum> nameMap = Enums.buildNameMap(values);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			Enums.fromString(INVALID_VALUE, nameMap, values);
		});

		assertThat(e.getMessage(), equalTo("'" + INVALID_VALUE + "' has no corresponding value. "
				+ "Accepted values: " + List.of(values)));
	}

	@ParameterizedTest
	@EnumSource(ExampleEnum.class)
	void shouldReturnTheEnumValueFromAStringWithFrom(final ExampleEnum exampleEnum) {
		ExampleEnum[] values = ExampleEnum.values();
		Map<String, ExampleEnum> nameMap = Enums.buildNameMap(values);

		ExampleEnum result = Enums.from(exampleEnum.toString(), nameMap, values);
		assertThat(result, equalTo(exampleEnum));
	}

	@ParameterizedTest
	@EnumSource(ExampleEnum.class)
	void shouldReturnTheEnumValueFromAStringWithFromString(final ExampleEnum exampleEnum) {
		ExampleEnum[] values = ExampleEnum.values();
		Map<String, ExampleEnum> nameMap = Enums.buildNameMap(values);

		ExampleEnum result = Enums.fromString(exampleEnum.toString(), nameMap, values);
		assertThat(result, equalTo(exampleEnum));
	}

	@ParameterizedTest
	@EnumSource(ExampleEnum.class)
	void shouldReturnTheEnumValueFromAStringWithFromStringWithDefaultValueSupplier(final ExampleEnum exampleEnum) {
		ExampleEnum[] values = ExampleEnum.values();
		Map<String, ExampleEnum> nameMap = Enums.buildNameMap(values);

		ExampleEnum result = Enums.fromString(exampleEnum.toString(), nameMap, () -> ExampleEnum.C);
		assertThat(result, equalTo(exampleEnum));
	}

	@ParameterizedTest
	@EnumSource(ExampleEnum.class)
	void shouldReturnTheDefaultValueFromAStringWithFromStringWithDefaultValueSupplier(final ExampleEnum exampleEnum) {
		Map<String, ExampleEnum> nameMap = Collections.emptyMap();

		ExampleEnum result = Enums.fromString(exampleEnum.toString(), nameMap, () -> ExampleEnum.C);
		assertThat(result, equalTo(ExampleEnum.C));
	}

	@ParameterizedTest
	@EnumSource(ExampleEnum.class)
	void shouldReturnValueOfForValidValues(final ExampleEnum exampleEnum) {
		ExampleEnum expected = Enums.valueOf(ExampleEnum.class, exampleEnum.name());
		assertThat(exampleEnum, equalTo(expected));
	}

	@Test
	void shouldReturnNullForNullParameterOnValueOf() {
		assertNull(Enums.valueOf(ExampleEnum.class, null));
	}

	@Test
	void shouldThrowExceptionForInvalidValueOnValueOf() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			Enums.valueOf(ExampleEnum.class, INVALID_VALUE);
		});

		assertThat(e.getMessage(), equalTo("Enum does not exist: " + ExampleEnum.class.getCanonicalName() + "." + INVALID_VALUE));
	}

	@Test
	void shouldThrowExceptionIfClassIsInstantiatedWithDefaultConstructor() {
		Throwable targetException = null;
		Constructor<Enums> defaultConstructor = Constructors.getDeclared(Enums.class);
		try {
			Constructors.IgnoreAccess.newInstance(defaultConstructor);
		} catch (ReflectionException e) {
			targetException = ((InvocationTargetException) e.getCause()).getTargetException();
			assertThat(targetException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
		assertTrue(targetException instanceof UnsupportedOperationException);
	}

	@Test
	void shouldReturnTheNameOfTheEnumWithSafeName() {
		String s = Enums.safeName(ExampleEnum.A);

		assertThat(s, equalTo(ExampleEnum.A.name()));
	}

	@Test
	void shouldReturnNullWithSafeNameIfTheParameterIsNull() {
		String s = Enums.safeName(null);

		assertThat(s, equalTo(null));
	}

}
