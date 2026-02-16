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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.testdata.A;

/**
 * Test class for {@link MemberAccessor}.
 *
 * @author Radu Sebastian LAZIN
 */
class MemberAccessorTest {

	private static final String FIELD_I = "i";

	private static final int INT_13 = 13;
	private static final int INT_11 = 11;

	@Test
	void shouldReleaseNonAccessibleFields() throws Exception {
		A a = new A();
		a.setI(INT_11);

		Field field = a.getClass().getDeclaredField(FIELD_I);

		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(a, field)) {
			field.set(a, INT_13);
		}

		assertFalse(field.canAccess(a));
	}

	@Test
	void shouldReleaseAccessibleFields() throws Exception {
		A a = new A();
		a.setI(INT_11);

		Field field = a.getClass().getDeclaredField(FIELD_I);
		field.setAccessible(true);

		try (MemberAccessor<Field> fieldAccessor = new MemberAccessor<>(a, field)) {
			field.set(a, INT_13);
		}

		assertTrue(field.canAccess(a));
	}

	@Test
	void shouldChangeAccessInConstructorAndChangeBackOnRelease() throws Exception {
		A a = new A();
		Field field = A.class.getDeclaredField(A.FIELD_NAME);

		assertFalse(field.canAccess(a));

		boolean isAccessible;
		try (MemberAccessor<Field> ignored = new MemberAccessor<>(a, field)) {
			isAccessible = field.canAccess(a);
		}

		assertTrue(isAccessible);
		assertFalse(field.canAccess(a));
	}

}
