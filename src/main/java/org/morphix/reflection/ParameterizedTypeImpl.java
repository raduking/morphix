package org.morphix.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link ParameterizedType}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ParameterizedTypeImpl implements ParameterizedType {

	private final Type rawType;

	private final Type[] arguments;

	private final Type ownerType;

	private ParameterizedTypeImpl(final Type rawType, final Type[] arguments, final Type ownerType) {
		this.rawType = rawType;
		this.arguments = arguments;
		this.ownerType = ownerType;
	}

	public static ParameterizedTypeImpl make(final Class<?> rawType, final Type[] actualTypeArguments, final Type ownerType) {
		return new ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
	}

	@Override
	public Type[] getActualTypeArguments() {
		return arguments;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof ParameterizedTypeImpl that) {
			return Objects.equals(that.rawType, this.rawType)
					&& Arrays.equals(that.arguments, this.arguments)
					&& Objects.equals(that.ownerType, this.ownerType);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return rawType.hashCode();
	}

}
