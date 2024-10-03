package org.morphix.reflection;

import java.lang.reflect.Constructor;

/**
 * Helper class for accessing constructors that are not accessible.
 *
 * @param <T> type for which the constructor refers to
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstructorAccessor<T> extends MemberAccessor<Constructor<T>> {

	public ConstructorAccessor(final Constructor<T> member) {
		super(null, member);
	}

}
