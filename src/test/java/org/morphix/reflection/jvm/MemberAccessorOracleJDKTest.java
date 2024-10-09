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
package org.morphix.reflection.jvm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link MemberAccessorOracleJDK}.
 *
 * @author Radu Sebastian LAZIN
 */
class MemberAccessorOracleJDKTest {

	private static final String FIELD_NAME_IMPL_LOOKUP = "IMPL_LOOKUP";
	private static final Set<String> EXCLUDED_FIELDS = Set.of("FIELD_NAME_IMPL_LOOKUP");

	@Test
	void shouldNotThrowExceptionIfInitFails() throws Exception {
		assumeTrue(MemberAccessorOracleJDK.isUsable());

		for (Field field : MemberAccessorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// also guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName) && Modifier.isStatic(field.getModifiers())) {
				Fields.IgnoreAccess.setStatic(MemberAccessorOracleJDK.class, fieldName, null);
			}
		}

		MemberAccessorOracleJDK.initialize("________");
		assertFalse(MemberAccessorOracleJDK.isUsable());

		List<Object> staticFieldsValues = new ArrayList<>();
		for (Field field : MemberAccessorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// also guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName) && Modifier.isStatic(field.getModifiers())) {
				Object value = Fields.IgnoreAccess.getStatic(MemberAccessorOracleJDK.class, fieldName);
				staticFieldsValues.add(value);
			}
		}
		assertTrue(staticFieldsValues.stream().allMatch(Objects::isNull));

		Class<?> sharedSecretsClass = Class.forName(ConstantPoolAccessorOracleJDK.SHARED_SECRETS_CLASS_NAME);
		Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");

		assertThrows(Exception.class, () -> {
			try (var methodAccessor = new MemberAccessorOracleJDK<>(javaLangAccessGetter)) {
				javaLangAccessGetter.invoke(null);
			}
		});

		MemberAccessorOracleJDK.initialize(FIELD_NAME_IMPL_LOOKUP);
	}

	@Test
	void shouldChangeAccessInConstructorAndChangeBackOnRelease() throws Exception {
		assumeTrue(MemberAccessorOracleJDK.isUsable());

		A a = new A();
		Field field = A.class.getDeclaredField(A.FIELD_NAME);

		assertFalse(field.canAccess(a));

		boolean isAccessible;
		try (var ignored = new MemberAccessorOracleJDK<>(field)) {
			isAccessible = field.canAccess(a);
		}

		assertTrue(isAccessible);
		assertFalse(field.canAccess(a));
	}

	@Test
	void shouldSetTheFieldNameImplLookupToCorrectValue() {
		assertThat(MemberAccessorOracleJDK.FIELD_NAME_IMPL_LOOKUP, equalTo(FIELD_NAME_IMPL_LOOKUP));
	}
}
