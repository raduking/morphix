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
package org.morphix.reflection;

import java.lang.reflect.Field;

import sun.misc.Unsafe; // NOSONAR

/**
 * {@link Unsafe} wrapper class.
 *
 * @author Radu Sebastian LAZIN
 */
public class TheUnsafe {

	static final String THE_UNSAFE_FIELD_NAME = "theUnsafe";

	static final Unsafe UNSAFE = getUnsafe(THE_UNSAFE_FIELD_NAME);

	static boolean isUnsafeAvailable() {
		return null != UNSAFE;
	}

	static Unsafe getUnsafe(final String theUnsafeFieldName) {
		Field theUnsafeField = Fields.getDeclaredFieldInHierarchy(Unsafe.class, theUnsafeFieldName);
		if (null == theUnsafeField) {
			return null;
		}
		try (MemberAccessor<Field> ignored = new MemberAccessor<>(null, theUnsafeField)) {
			return Fields.get(null, theUnsafeField);
		}
	}

	@SuppressWarnings("deprecation")
	public static long staticFieldOffset(final Field field) {
		return UNSAFE.staticFieldOffset(field);
	}

	@SuppressWarnings("deprecation")
	public static long objectFieldOffset(final Field field) {
		return UNSAFE.objectFieldOffset(field);
	}

	@SuppressWarnings("deprecation")
	public static Object staticFieldBase(final Field field) {
		return UNSAFE.staticFieldBase(field);
	}

	public static void putObject(final Object obj, final long offset, final Object value) {
		UNSAFE.putObject(obj, offset, value);
	}

	private TheUnsafe() {
		throw Constructors.unsupportedOperationException();
	}
}