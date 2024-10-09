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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

}
