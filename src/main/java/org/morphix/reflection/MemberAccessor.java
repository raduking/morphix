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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Helper class for accessing members that are not accessible. This is only a read access meaning that it cannot write
 * {@code final} fields.
 *
 * @param <T> an {@link AccessibleObject} or {@link Member} type
 *
 * @author Radu Sebastian LAZIN
 */
public class MemberAccessor<T extends AccessibleObject & Member> implements AutoCloseable {

	/**
	 * Error message.
	 */
	protected static final String ACCESS_CHANGE_ERROR = "Could not change access to member: ";

	/**
	 * The member which will have its access changed temporarily.
	 */
	private final T member;

	/**
	 * The flag that says if the field is accessible or not.
	 */
	private final boolean isAccessible;

	/**
	 * Constructor with all parameters.
	 *
	 * @param object object containing the member to access
	 * @param member member to access
	 */
	public MemberAccessor(final Object object, final T member) {
		this.member = Objects.requireNonNull(member, "member");
		// by default, a reflected object is not accessible
		Object actual = null != object && Modifier.isStatic(member.getModifiers()) ? null : object;

		this.isAccessible = ReflectionException.wrapThrowing(() -> member.canAccess(actual), ACCESS_CHANGE_ERROR + member);
		if (!isAccessible) {
			ReflectionException.wrapThrowing(() -> member.setAccessible(true), ACCESS_CHANGE_ERROR + member); // NOSONAR
		}
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() {
		if (!isAccessible) {
			ReflectionException.wrapThrowing(() -> member.setAccessible(false), ACCESS_CHANGE_ERROR + member);
		}
	}
}
