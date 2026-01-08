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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Objects;

import org.morphix.lang.JavaObjects;
import org.morphix.reflection.AccessSetter;
import org.morphix.reflection.Constructors;

/**
 * This class is used to make accessible some methods which normally are not accessible. From Java 12,
 * {@link AccessibleObject}.override was added to the reflection blacklist. Access modification can still be
 * circumvented by using the Unsafe technique to get the implementation lookup from {@link MethodHandles}.
 *
 * @param <T> accessible object type
 *
 * @author Radu Sebastian LAZIN
 */
public class MemberAccessorOracleJDK<T extends AccessibleObject & Member> implements AutoCloseable {

	/**
	 * IMPL_LOOKUP field name.
	 */
	protected static final String FIELD_NAME_IMPL_LOOKUP = "IMPL_LOOKUP";

	/**
	 * overrideSetter method handle.
	 */
	private static MethodHandle overrideSetter = null;

	/**
	 * Statically initialize the overrideSetter.
	 */
	static {
		initialize(FIELD_NAME_IMPL_LOOKUP);
	}

	/**
	 * The access setter.
	 */
	private final AccessSetter<T> accessSetter;

	/**
	 * The member for which the access needs to be changed.
	 */
	private final T member;

	/**
	 * Initializes the override setter.
	 *
	 * @param implLookupFieldName the {@link Lookup} implementation field name
	 */
	protected static void initialize(final String implLookupFieldName) {
		try {
			var lookupClass = MethodHandles.Lookup.class;
			var lookupConstructor = Constructors.getDeclared(lookupClass, Class.class);
			var lookup = Constructors.IgnoreAccess.newInstance(lookupConstructor, lookupClass);

			var varHandle = lookup.findStaticVarHandle(lookupClass, implLookupFieldName, lookupClass);
			MethodHandles.Lookup implLookup = JavaObjects.cast(varHandle.get());

			overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
		} catch (Throwable swallow) { // NOSONAR
			// swallow exception since it just won't initialize the overrideSetter if
			// the actual JDK/JRE doesn't support it this way
		}
	}

	/**
	 * Returns true if this member accessor is usable to change field access.
	 *
	 * @return true if this member accessor is usable, false otherwise
	 */
	public static boolean isUsable() {
		return null != overrideSetter;
	}

	/**
	 * Constructor with the member to change access.
	 *
	 * @param member member
	 */
	public MemberAccessorOracleJDK(final T member) {
		this.member = Objects.requireNonNull(member, "member");
		accessSetter = AccessSetter.ofOverride(overrideSetter);
		accessSetter.setAccessible(member, true);
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() {
		accessSetter.setAccessible(member, false);
	}

}
