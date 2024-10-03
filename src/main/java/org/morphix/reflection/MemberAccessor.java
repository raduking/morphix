package org.morphix.reflection;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Helper class for accessing members that are not accessible.
 * <p>
 * TODO: allow final fields also.
 * </p>
 *
 * @param <T> an {@link AccessibleObject} or {@link Member} type
 *
 * @author Radu Sebastian LAZIN
 */
public class MemberAccessor<T extends AccessibleObject & Member> implements AutoCloseable {

	private static final String ACCESS_CHANGE_ERROR = "Could not change access to member: ";

	private final T member;
	private final boolean isAccessible;

	public MemberAccessor(final Object object, final T member) {
		this.member = requireNonNull(member, "member");
		// by default a reflected object is not accessible
		Object actual = null != object && Modifier.isStatic(member.getModifiers()) ? null : object;
		this.isAccessible = ReflectionException.wrapThrowing(() -> member.canAccess(actual), ACCESS_CHANGE_ERROR + member);

		if (!isAccessible) {
			ReflectionException.wrapThrowing(() -> member.setAccessible(true), ACCESS_CHANGE_ERROR + member); // NOSONAR
		}
	}

	@Override
	public void close() {
		if (!isAccessible) {
			ReflectionException.wrapThrowing(() -> member.setAccessible(false), ACCESS_CHANGE_ERROR + member);
		}
	}

}
