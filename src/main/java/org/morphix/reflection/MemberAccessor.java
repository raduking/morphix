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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.morphix.lang.Unchecked;
import org.morphix.lang.function.Runnables;

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
		this.isAccessible = isAccessible(object, member);
		if (!isAccessible) {
			setAccessible(true);
		}
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() {
		if (!isAccessible) {
			setAccessible(false);
		}
	}

	/**
	 * Sets the accessibility of the member to the given value.
	 *
	 * @param accessible {@code true} to make the member accessible, {@code false} to make it not accessible
	 */
	public void setAccessible(final boolean accessible) {
		setAccessible(member, accessible);
	}

	/**
	 * Checks if the member is accessible for the given object.
	 *
	 * @param <T> the type of the member, which must be both an {@link AccessibleObject} and a {@link Member}
	 *
	 * @param obj object containing the member to access
	 * @param member member to check
	 * @return {@code true} if the member is accessible, {@code false} otherwise
	 */
	public static <T extends AccessibleObject & Member> boolean isAccessible(final Object obj, final T member) {
		Object target = null != obj && JavaModifier.STATIC.isPresentOn(member) ? null : obj;
		return ReflectionException.wrapThrowing(() -> member.canAccess(target), ACCESS_CHANGE_ERROR + member);
	}

	/**
	 * Sets the accessibility of the member.
	 *
	 * @param <T> the type of the member, which must be both an {@link AccessibleObject} and a {@link Member}
	 *
	 * @param member member to set accessibility for
	 * @param accessible {@code true} to make the member accessible, {@code false} to make it not accessible
	 */
	public static <T extends AccessibleObject & Member> void setAccessible(final T member, final boolean accessible) {
		ReflectionException.wrapThrowing(() -> member.setAccessible(accessible), ACCESS_CHANGE_ERROR + member); // NOSONAR
	}

	/**
	 * Executes the given supplier with the member accessible. If the member is already accessible, it will execute the
	 * supplier without changing the accessibility. Otherwise, it will change the accessibility to {@code true} before
	 * executing the supplier and will restore the original accessibility after execution.
	 * <p>
	 * Any Exception thrown during the execution of the supplier will be re-thrown as an unchecked exception.
	 *
	 * @param <T> the type of the member, which must be both an {@link AccessibleObject} and a {@link Member}
	 * @param <U> the type of the result returned by the supplier
	 *
	 * @param obj object containing the member to access
	 * @param member member to access
	 * @param supplier supplier to execute with the member accessible
	 * @return the result returned by the supplier
	 */
	public static <T extends AccessibleObject & Member, U> U on(final Object obj, final T member, final Supplier<U> supplier) {
		return on(obj, member, supplier, Unchecked.Undeclared::reThrow);
	}

	/**
	 * Executes the given supplier with the member accessible. If the member is already accessible, it will execute the
	 * supplier without changing the accessibility. Otherwise, it will change the accessibility to {@code true} before
	 * executing the supplier and will restore the original accessibility after execution.
	 * <p>
	 * If any {@link ReflectionException} exception is thrown during the execution of the supplier, it will be handled by
	 * the given exception handler.
	 *
	 * @param <T> the type of the member, which must be both an {@link AccessibleObject} and a {@link Member}
	 * @param <U> the type of the result returned by the supplier
	 *
	 * @param obj object containing the member to access
	 * @param member member to access
	 * @param supplier supplier to execute with the member accessible
	 * @param onError handler for any exception thrown during the execution of the supplier
	 * @return the result returned by the supplier or by the exception handler if an exception is thrown
	 */
	public static <T extends AccessibleObject & Member, U> U on(final Object obj, final T member, final Supplier<U> supplier,
			final Function<ReflectionException, U> onError) {
		try {
			if (isAccessible(obj, member)) {
				return supplier.get();
			}
			try {
				setAccessible(member, true);
				return supplier.get();
			} finally {
				setAccessible(member, false);
			}
		} catch (ReflectionException e) {
			return onError.apply(e);
		}
	}

	/**
	 * Executes the given runnable with the member accessible. If the member is already accessible, it will execute the
	 * runnable without changing the accessibility. Otherwise, it will change the accessibility to {@code true} before
	 * executing the runnable and will restore the original accessibility after execution.
	 * <p>
	 * If any {@link ReflectionException} exception is thrown during the execution of the runnable, it will be handled by
	 * the given exception handler.
	 *
	 * @param <T> the type of the member, which must be both an {@link AccessibleObject} and a {@link Member}
	 *
	 * @param obj object containing the member to access
	 * @param member member to access
	 * @param runnable runnable to execute with the member accessible
	 * @param onError handler for any exception thrown during the execution of the runnable
	 */
	public static <T extends AccessibleObject & Member> void on(final Object obj, final T member, final Runnable runnable,
			final Consumer<ReflectionException> onError) {
		on(obj, member, Runnables.toSupplier(runnable), t -> {
			onError.accept(t);
			return null;
		});
	}
}
